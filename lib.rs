//! Native Rust experiments are intentionally not loaded by the Minecraft mod yet.
//! A native library will only be shipped after benchmarks show a real benefit and
//! platform-specific packaging/testing is in place.

#[no_mangle]
pub extern "C" fn ultraboost_native_version() -> u32 {
    1
}
