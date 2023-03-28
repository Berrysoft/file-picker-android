fn main() {
    tauri_build::mobile::PluginBuilder::new()
        .android_path("android")
        .run()
        .unwrap();
}
