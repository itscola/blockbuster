package mchorse.blockbuster.model_editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import mchorse.blockbuster.model_editor.modal.GuiAlertModal;
import mchorse.blockbuster.model_editor.modal.GuiInputModal;
import mchorse.blockbuster.model_editor.modal.GuiLimbsList;
import mchorse.blockbuster.model_editor.modal.GuiLimbsList.ILimbPicker;
import mchorse.blockbuster.model_editor.modal.GuiListViewer;
import mchorse.blockbuster.model_editor.modal.GuiListViewer.IListResponder;
import mchorse.blockbuster.model_editor.modal.GuiModal;
import mchorse.blockbuster.model_editor.modal.IModalCallback;
import mchorse.metamorph.api.models.Model;
import mchorse.metamorph.api.models.Model.Limb;
import mchorse.metamorph.client.model.ModelCustom;
import mchorse.metamorph.client.model.parsing.ModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Model editor GUI
 *
 * This GUI
 */
public class GuiModelEditor extends GuiScreen implements IModalCallback, IListResponder, ILimbPicker
{
    /**
     * Currently data model which we are editing
     */
    private Model data;

    /**
     * Compiled data model which we are currently editing
     */
    private ModelCustom model;

    /**
     * Cached texture path
     */
    private ResourceLocation textureRL;

    /**
     * Current modal
     */
    public GuiModal currentModal;

    /* GUI fields */

    /**
     * Available poses
     */
    private GuiListViewer poses;

    /**
     * Limbs sidebar
     */
    private GuiLimbsList limbs;

    /**
     * Texture path field
     */
    private GuiTextField texture;

    /**
     * Pose field
     */
    private GuiButton pose;

    /**
     * Save button, this will prompt user to choose a name
     */
    private GuiButton save;

    /**
     * Create clean, new, model out of existing ones or
     */
    private GuiButton clean;

    /**
     * Setup by default the
     */
    public GuiModelEditor()
    {
        this.poses = new GuiListViewer(null, this);
        this.limbs = new GuiLimbsList(this);
        this.setupModel(ModelCustom.MODELS.get("blockbuster.steve"));
    }

    /**
     * Setup the model
     */
    private void setupModel(ModelCustom model)
    {
        this.data = ModelUtils.cloneModel(model.model);

        List<String> poses = new ArrayList<String>();
        poses.addAll(this.data.poses.keySet());

        this.poses.setStrings(poses);
        this.limbs.setModel(this.data);
        this.buildModel();
    }

    /**
     * Build the model from data model
     */
    private void buildModel()
    {
        try
        {
            this.model = new ModelParser().parseModel(this.data, ModelCustom.class);
            this.changePose("standing");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Change pose
     */
    private void changePose(String pose)
    {
        this.model.pose = this.data.getPose(pose);

        if (this.pose != null)
        {
            this.pose.displayString = pose;
        }
    }

    @Override
    public void initGui()
    {
        /* Initiate the texture field */
        this.texture = new GuiTextField(0, this.fontRendererObj, 11, this.height - 24, 98, 18);
        this.texture.setMaxStringLength(400);
        this.texture.setText("blockbuster.actors:steve/Walter");
        this.textureRL = new ResourceLocation(this.texture.getText());

        /* Buttons */
        this.save = new GuiButton(0, this.width - 60, 5, 50, 20, "Save");
        this.clean = new GuiButton(1, this.width - 115, 5, 50, 20, "New");
        this.pose = new GuiButton(2, this.width - 90, this.height - 25, 80, 20, "standing");

        this.buttonList.add(this.save);
        this.buttonList.add(this.clean);
        this.buttonList.add(this.pose);

        this.poses.updateRect(this.width - 90, this.height - 106, 80, 80);
        this.poses.setHidden(true);

        this.limbs.updateRect(this.width - 91, 47, 82, this.height - 47 - 30);

        if (this.currentModal != null)
        {
            this.currentModal.initiate();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.currentModal = new GuiInputModal(this, this.fontRendererObj);
            this.currentModal.label = "Say my name?";
            this.currentModal.initiate();
        }
        else if (button.id == 1)
        {
            /* New one */
        }
        else if (button.id == 2)
        {
            this.poses.setHidden(false);
        }
    }

    /**
     * It's like {@link #actionPerformed(GuiButton)}, but only comes from modal
     * windows.
     */
    @Override
    public void modalButtonPressed(GuiModal modal, GuiButton button)
    {
        if (button.id == -1)
        {
            this.currentModal = null;
        }
        else if (button.id == -2)
        {
            String input = ((GuiInputModal) this.currentModal).getInput();

            this.currentModal = new GuiAlertModal(this, this.fontRendererObj);
            this.currentModal.label = "Are you sure you want to ratched and clank with me, " + input + "?";
            this.currentModal.initiate();
        }
    }

    @Override
    public void pickedValue(String value)
    {
        this.changePose(value);
    }

    @Override
    public void pickLimb(Limb limb)
    {
        System.out.println(limb);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.currentModal == null)
        {
            this.texture.textboxKeyTyped(typedChar, keyCode);

            if (this.texture.isFocused() && !this.texture.getText().equals(this.textureRL.toString()))
            {
                this.textureRL = new ResourceLocation(this.texture.getText());
            }
        }
        else
        {
            this.currentModal.keyTyped(typedChar, keyCode);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();

        if (!this.poses.isInside(i, j))
        {
            this.limbs.handleMouseInput();
        }

        this.poses.handleMouseInput();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        super.setWorldAndResolution(mc, width, height);
        this.poses.setWorldAndResolution(mc, width, height);
        this.limbs.setWorldAndResolution(mc, width, height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.poses.isInside(mouseX, mouseY))
        {
            return;
        }

        if (this.currentModal == null)
        {
            this.texture.mouseClicked(mouseX, mouseY, mouseButton);

            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            this.currentModal.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Draw the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.fontRendererObj.drawStringWithShadow("Model Editor", 10, 10, 0xffffff);
        this.fontRendererObj.drawStringWithShadow("Limbs", this.width - 85, 35, 0xffffff);

        this.texture.drawTextBox();

        /* Draw the model */
        float scale = this.height / 3;
        float x = this.width / 2;
        float y = this.height / 2 + scale * 1.1F;
        float yaw = (x - mouseX) / this.width * 90;
        float pitch = (y + scale + mouseY) / this.height * 90 - 135;

        this.drawModel(x, y, scale, yaw, pitch);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.limbs.drawScreen(mouseX, mouseY, partialTicks);
        this.poses.drawScreen(mouseX, mouseY, partialTicks);

        /* Draw current modal */
        if (this.currentModal != null)
        {
            this.currentModal.drawModal(mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draw currently edited model
     */
    private void drawModel(float x, float y, float scale, float yaw, float pitch)
    {
        EntityPlayer player = this.mc.thePlayer;
        float factor = 0.0625F;

        this.mc.renderEngine.bindTexture(this.textureRL);

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        RenderHelper.enableStandardItemLighting();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        GlStateManager.enableAlpha();

        this.model.setLivingAnimations(player, 0, 0, 0);
        this.model.setRotationAngles(0, 0, player.ticksExisted, yaw, pitch, factor, player);

        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.model.render(player, 0, 0, 0, 0, 0, factor);

        GlStateManager.disableDepth();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}