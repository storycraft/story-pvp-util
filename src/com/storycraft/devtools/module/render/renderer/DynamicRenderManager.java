package com.storycraft.devtools.module.render.renderer;

import com.storycraft.devtools.module.render.DynamicBoundingBox;
import com.storycraft.devtools.util.reflect.Reflect;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DynamicRenderManager extends RenderManager {

    private DynamicBoundingBox dynamicBoundingBox;

    private boolean isAimHighlightEnabled;
    private int boundingBoxDistance;
    private boolean isHideRequired;
    private boolean isHideNearOrFar;
    private boolean isEyeSightDrawingEnabled;
    private boolean isEyePosDrawingEnabled;
    private boolean isProjectileBoundingBoxEnabled;
    private boolean isNonLivingBoundingBoxEnabled;

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e){
        updateSettings();
    }

    public void updateSettings(){
        this.isAimHighlightEnabled = dynamicBoundingBox.isAimHighlightEnabled();
        this.boundingBoxDistance = dynamicBoundingBox.getBoundingBoxDistance();
        this.isHideRequired = dynamicBoundingBox.isHideRequired();
        this.isHideNearOrFar = dynamicBoundingBox.isHideNearOrFar();
        this.isEyeSightDrawingEnabled = dynamicBoundingBox.isEyeSightDrawingEnabled();
        this.isEyePosDrawingEnabled = dynamicBoundingBox.isEyePosDrawingEnabled();
        this.isProjectileBoundingBoxEnabled = dynamicBoundingBox.isProjectileBoundingBoxEnabled();
        this.isNonLivingBoundingBoxEnabled = dynamicBoundingBox.isNonLivingBoundingBoxEnabled();
    }

    private static Reflect.WrappedField<Boolean, RenderManager> renderOutlines;

    static {
        renderOutlines = Reflect.getField(RenderManager.class, "renderOutlines", "field_178639_r");
    }

    public DynamicRenderManager(TextureManager renderEngineIn, RenderItem itemRendererIn, DynamicBoundingBox dynamicBoundingBox) {
        super(renderEngineIn, itemRendererIn);

        this.dynamicBoundingBox = dynamicBoundingBox;
    }

    @Override
    public boolean doRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_)
    {
        Render<Entity> render = null;

        try
        {
            render = getEntityRenderObject(entity);
            boolean renderOutLine = renderOutlines.get(this);

            if (render != null && renderEngine != null)
            {
                try
                {
                    if (render instanceof RendererLivingEntity)
                    {
                        ((RendererLivingEntity)render).setRenderOutlines(renderOutLine);
                    }

                    render.doRender(entity, x, y, z, entityYaw, partialTicks);
                }
                catch (Throwable throwable2)
                {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable2, "Rendering entity in world"));
                }

                try
                {
                    if (!renderOutLine)
                    {
                        render.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
                    }
                }
                catch (Throwable throwable1)
                {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Post-rendering entity in world"));
                }

                if (isDebugBoundingBox() && !entity.isInvisible() && !p_147939_10_)
                {
                    try
                    {
                        if (entity instanceof EntityLiving || entity instanceof EntityOtherPlayerMP || isProjectileBoundingBoxEnabled && entity instanceof IProjectile || isNonLivingBoundingBoxEnabled) {
                            if (isHideRequired) {
                                if (!entity.isEntityAlive())
                                    return true;

                                int limit = dynamicBoundingBox.getBoundingBoxDistance();
                                int sqLimit = limit * limit;
                                double distanceSq = livingPlayer.getDistanceSqToEntity(entity);

                                if (isHideNearOrFar) {
                                    if (distanceSq < sqLimit)
                                        return true;
                                } else {
                                    if (distanceSq > sqLimit)
                                        return true;
                                }
                            }

                            renderDebugBoundingBox(entity, x, y, z, entityYaw, partialTicks);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity hitbox in world"));
                    }
                }
            }
            else if (this.renderEngine != null)
            {
                return false;
            }

            return true;
        }
        catch (Throwable throwable3)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entity.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", render);
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", Float.valueOf(entityYaw));
            crashreportcategory1.addCrashSection("Delta", Float.valueOf(partialTicks));
            throw new ReportedException(crashreport);
        }
    }

    private void renderDebugBoundingBox(Entity entityIn, double p_85094_2_, double p_85094_4_, double p_85094_6_, float p_85094_8_, float p_85094_9_)
    {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        float f = entityIn.width / 2.0F;

        AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - entityIn.posX + p_85094_2_, axisalignedbb.minY - entityIn.posY + p_85094_4_, axisalignedbb.minZ - entityIn.posZ + p_85094_6_, axisalignedbb.maxX - entityIn.posX + p_85094_2_, axisalignedbb.maxY - entityIn.posY + p_85094_4_, axisalignedbb.maxZ - entityIn.posZ + p_85094_6_);

        boolean red = isAimHighlightEnabled && entityIn == pointedEntity;

        if (red){
            RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, 255, 0, 0, 255);
        }
        else{
            RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, 255, 255, 255, 255);
        }

        if (isEyePosDrawingEnabled && entityIn instanceof EntityLivingBase)
        {
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(p_85094_2_ - f, p_85094_4_ + entityIn.getEyeHeight() - 0.009999999776482582D, p_85094_6_ - f, p_85094_2_ + f, p_85094_4_ + entityIn.getEyeHeight() + 0.009999999776482582D, p_85094_6_ + f), 255, 0, 0, 255);
        }

        if (isEyeSightDrawingEnabled) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            Vec3 vec3 = entityIn.getLook(p_85094_9_);
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(p_85094_2_, p_85094_4_ +  entityIn.getEyeHeight(), p_85094_6_).color(0, 0, 255, 255).endVertex();
            worldrenderer.pos(p_85094_2_ + vec3.xCoord * 2.0D, p_85094_4_ +  entityIn.getEyeHeight() + vec3.yCoord * 2.0D, p_85094_6_ + vec3.zCoord * 2.0D).color(0, 0, 255, 255).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}
