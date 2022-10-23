package cmu.plugins.renderers;

import cmu.shaders.BaseRenderPlugin;
import cmu.shaders.ShaderProgram;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;

public class RingTextureRenderer extends BaseRenderPlugin {
    @Override
    protected int[] initBuffers() {
        return new int[0];
    }

    @Override
    protected void populateUniforms(int glProgramID, CombatEngineLayers layer, ViewportAPI viewport) {

    }

    @Override
    protected void updateBuffers(int[] buffers, CombatEngineLayers layer, ViewportAPI viewport) {

    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {

    }

    @Override
    protected ShaderProgram initShaderProgram() {
        return null;
    }
}
