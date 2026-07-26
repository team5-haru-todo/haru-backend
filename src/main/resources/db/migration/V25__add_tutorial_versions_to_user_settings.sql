ALTER TABLE user_settings
    ADD COLUMN main_tutorial_version INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN main_completed_tutorial_version INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_user_settings_main_tutorial_version
        CHECK (main_tutorial_version >= 0),
    ADD CONSTRAINT chk_user_settings_main_completed_tutorial_version
        CHECK (main_completed_tutorial_version >= 0);
