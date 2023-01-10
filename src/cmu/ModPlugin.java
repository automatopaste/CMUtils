package cmu;

import cmu.misc.SpecLoadingUtils;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class ModPlugin extends BaseModPlugin {

    public static SpriteAPI PARAGON;

    @Override
    public void onApplicationLoad() throws Exception {
        SpecLoadingUtils.loadSubsystemData();

        Global.getSettings().loadTexture("graphics/CMUtils/paragon_mask.png");
        PARAGON = Global.getSettings().getSprite("graphics/CMUtils/paragon_mask.png");
    }
}