fn main() {
    tauri_plugin::Builder::new(&["pickFiles"])
        .android_path("android")
        .build();
}
