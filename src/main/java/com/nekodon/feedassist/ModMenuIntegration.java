package com.nekodon.feedassist;

import com.nekodon.feedassist.client.FeedAssistConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FeedAssistConfigScreen::new;
    }
}

