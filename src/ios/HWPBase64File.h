#import <Cordova/CDV.h>

@interface HWPBase64File : CDVPlugin

- (void) save:(CDVInvokedUrlCommand*)command;
- (void) load:(CDVInvokedUrlCommand*)command;
- (void) open:(CDVInvokedUrlCommand*)command;
- (void) launchNavigation:(CDVInvokedUrlCommand *)command;
- (void) watermarkImage:(CDVInvokedUrlCommand *)command;
@end