#if canImport(Testing)
import Testing
import OpentelemetrySdk

@Suite struct OpentelemetrySdkExportTests {
    @Test func testSwiftModuleLoads() throws {
        #expect(Bool(true), "OpentelemetrySdk swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import OpentelemetrySdk

final class OpentelemetrySdkExportTests: XCTestCase {
    func testSwiftModuleLoads() {
        XCTAssertTrue(true)
    }
}
#endif


