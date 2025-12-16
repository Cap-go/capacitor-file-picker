import XCTest
@testable import CapgoFilePickerPlugin

final class CapgoFilePickerPluginTests: XCTestCase {
    func testPluginLoads() throws {
        let plugin = CapgoFilePickerPlugin()
        XCTAssertNotNil(plugin)
        XCTAssertEqual(plugin.identifier, "CapgoFilePickerPlugin")
        XCTAssertEqual(plugin.jsName, "CapgoFilePicker")
    }
}
