use serde::Deserialize;
use std::path::PathBuf;
use tauri::{
    plugin::{mobile::PluginInvokeError, Builder, PluginHandle, TauriPlugin},
    Manager, Runtime,
};

pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("file-picker-android")
        .setup(|app, api| {
            #[cfg(target_os = "android")]
            {
                let handle =
                    api.register_android_plugin("com.plugin.berrysoft.picker", "PickerPlugin")?;
                app.manage(PickerPlugin(handle));
            }
            Ok(())
        })
        .build()
}

#[derive(Debug, Deserialize)]
struct PickFilesResult {
    paths: Vec<PathBuf>,
}

pub struct PickerPlugin<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> PickerPlugin<R> {
    pub fn pick_files(&self) -> Result<Vec<PathBuf>, PluginInvokeError> {
        Ok(self
            .0
            .run_mobile_plugin::<PickFilesResult>("pickFiles", ())?
            .paths)
    }
}
