package cmu;

import cmu.misc.SpecLoadingUtils;
import com.fs.starfarer.api.BaseModPlugin;

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        SpecLoadingUtils.loadSubsystemData();
    }
}