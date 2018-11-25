package com.storycraft.pvputil.module.render.renderer;

import com.storycraft.pvputil.module.render.DynamicBoundingBox;
import com.storycraft.pvputil.util.reflect.Reflect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DynamicRenderManager extends RenderManager {

    private static Reflect.WrappedField<Boolean, RenderManager> renderOutlines;

    static {
        renderOutlines = Reflect.getField(RenderManager.class, "renderOutlines", "field_178639_r");
    }

    private DynamicBoundingBox dynamicBoundingBox;

    private boolean isAimHighlightEnabled;
    private int boundingBoxDistance;
    private boolean isHideRequired;
    private boolean isHideNearOrFar;
    private boolean isEyeSightDrawingEnabled;
    private boolean isEyePosDrawingEnabled;
    private boolean isPartDrawingEnabled;
    private boolean isProjectileBoundingBoxEnabled;
    private boolean isNonLivingBoundingBoxEnabled;

    public DynamicRenderManager(TextureManager renderEngineIn, RenderItem itemRendererIn, DynamicBoundingBox dynamicBoundingBox) {
        super(renderEngineIn, itemRendererIn);

        this.dynamicBoundingBox = dynamicBoundingBox;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e){
        updateSettings();
    }

    public void updateSettings(){
        this.isAimHighlightEnabled = dynamicBoundingBox.isAimHighlightEnabled();
        this.boundingBoxDistance = dynamicBoundingBox.getBoundingBoxDistance();
        this.isHideRequired = dynamicBoundingBox.isHideRequired();
        this.isHideNearOrFar = dynamicBoundingBox.isHideNearOrFar();
        this.isEyeSightDrawingEnabled = dynamicBoundingBox.isEyeSightDrawingEnabled();
        this.isEyePosDrawingEnabled = dynamicBoundingBox.isEyePosDrawingEnabled();
        this.isPartDrawingEnabled = dynamicBoundingBox.isPartDrawingEnabled();
        this.isProjectileBoundingBoxEnabled = dynamicBoundingBox.isProjectileBoundingBoxEnabled();
        this.isNonLivingBoundingBoxEnabled = dynamicBoundingBox.isNonLivingBoundingBoxEnabled();
    }

    @Override
    public void renderEntity(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_147939_10_)
    {
        Render<Entity> render = null;

        try
        {
            render = this.<Entity>getEntityRenderObject(entityIn);
            boolean renderOutlines = DynamicRenderManager.renderOutlines.get(this);

            if (render != null && this.renderEngine != null)
            {
                try
                {
                    render.setRenderOutlines(renderOutlines);
                    render.doRender(entityIn, x, y, z, yaw, partialTicks);
                }
                catch (Throwable throwable1)
                {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
                }

                try
                {
                    if (!renderOutlines)
                    {
                        render.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
                    }
                }
                catch (Throwable throwable2)
                {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable2, "Post-rendering entity in world"));
                }

                if (isDebugBoundingBox() && !entityIn.isInvisible() && !p_147939_10_)
                {
                    try
                    {
                        if (entityIn instanceof EntityLiving || entityIn instanceof EntityOtherPlayerMP || isProjectileBoundingBoxEnabled && entityIn instanceof IProjectile || isNonLivingBoundingBoxEnabled) {
                            if (isHideRequired && super.renderViewEntity != null) {
                                int sqLimit = boundingBoxDistance * boundingBoxDistance;
                                double distanceSq = super.renderViewEntity.getDistanceSq(entityIn.posX, entityIn.posY, entityIn.posZ);

                                if (isHideNearOrFar) {
                                    if (distanceSq < sqLimit)
                                        return;
                                } else {
                                    if (distanceSq > sqLimit)
                                        return;
                                }
                            }

                            renderDebugBoundingBox(entityIn, x, y, z, yaw, partialTicks);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity hitbox in world"));
                    }
                }
            }
        }
        catch (Throwable throwable3)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entityIn.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", render);
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", Float.valueOf(yaw));
            crashreportcategory1.addCrashSection("Delta", Float.valueOf(partialTicks));
            throw new ReportedException(crashreport);
        }
    }

    private void renderDebugBoundingBox(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        float f = entityIn.width / 2.0F;
        AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();

        boolean red = isAimHighlightEnabled && entityIn == pointedEntity;

        if (red) {
            RenderGlobal.drawBoundingBox(axisalignedbb.minX - entityIn.posX + x, axisalignedbb.minY - entityIn.posY + y, axisalignedbb.minZ - entityIn.posZ + z, axisalignedbb.maxX - entityIn.posX + x, axisalignedbb.maxY - entityIn.posY + y, axisalignedbb.maxZ - entityIn.posZ + z, 1.0F, 0.0F, 0.0F, 1.0F);
        }
        else {
            RenderGlobal.drawBoundingBox(axisalignedbb.minX - entityIn.posX + x, axisalignedbb.minY - entityIn.posY + y, axisalignedbb.minZ - entityIn.posZ + z, axisalignedbb.maxX - entityIn.posX + x, axisalignedbb.maxY - entityIn.posY + y, axisalignedbb.maxZ - entityIn.posZ + z, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        Entity[] aentity = entityIn.getParts();

        if (aentity != null && isPartDrawingEnabled)
        {

            for (Entity entity : aentity)
            {
                double d0 = (entity.posX - entity.prevPosX) * (double)partialTicks;
                double d1 = (entity.posY - entity.prevPosY) * (double)partialTicks;
                double d2 = (entity.posZ - entity.prevPosZ) * (double)partialTicks;
                AxisAlignedBB axisalignedbb1 = entity.getEntityBoundingBox();
                RenderGlobal.drawBoundingBox(axisalignedbb1.minX + x, axisalignedbb1.minY + y, axisalignedbb1.minZ + z, axisalignedbb1.maxX + x, axisalignedbb1.maxY + y, axisalignedbb1.maxZ + z, 0.25F, 1.0F, 0.0F, 1.0F);
            }
        }

        if (isEyePosDrawingEnabled && entityIn instanceof EntityLivingBase)
        {
            float f1 = 0.01F;
            RenderGlobal.drawBoundingBox(x - (double)f, y + (double)entityIn.getEyeHeight() - 0.009999999776482582D, z - (double)f, x + (double)f, y + (double)entityIn.getEyeHeight() + 0.009999999776482582D, z + (double)f, 1.0F, 0.0F, 0.0F, 1.0F);
        }

        if (isEyeSightDrawingEnabled) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            Vec3d vec3d = entityIn.getLook(partialTicks);
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y + (double)entityIn.getEyeHeight(), z).color(0, 0, 255, 255).endVertex();
            bufferbuilder.pos(x + vec3d.x * 2.0D, y + (double)entityIn.getEyeHeight() + vec3d.y * 2.0D, z + vec3d.z * 2.0D).color(0, 0, 255, 255).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}
