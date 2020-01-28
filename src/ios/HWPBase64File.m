#import "HWPBase64File.h"

@implementation HWPBase64File

- (void)save:(CDVInvokedUrlCommand*)command
{

    NSString* callbackId = [command callbackId];
    NSString* base64Data = [[command arguments] objectAtIndex:0];
    NSString* filename = [[command arguments] objectAtIndex:1];
    NSString* folder = [[command arguments] objectAtIndex:2];
    NSString* fullpath = [folder stringByAppendingString:filename];

    NSData *encData = [[NSData alloc] initWithBase64EncodedString:base64Data options:0];

    [encData writeToFile:fullpath atomically:YES];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fullpath];


    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)load:(CDVInvokedUrlCommand*)command
{

    NSString* callbackId = [command callbackId];

    NSString* fullpath = [[command arguments] objectAtIndex:0];
    NSData *nsFileData = [NSData dataWithContentsOfFile:fullpath];
    NSString *encodedString = [nsFileData base64Encoding];


    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:encodedString];

    //[self success:result callbackId:callbackId];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)open:(CDVInvokedUrlCommand*)command
{

    NSString* callbackId = [command callbackId];

    NSString* fullpath = [[command arguments] objectAtIndex:0];
    NSData *nsFileData = [NSData dataWithContentsOfFile:fullpath];
    NSString *encodedString = [nsFileData base64Encoding];


    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:encodedString];

    //[self success:result callbackId:callbackId];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)watermarkImage:(CDVInvokedUrlCommand *)command
{
    NSDictionary *json = [command.arguments objectAtIndex:0];
    NSString *fileName = [json objectForKey:@"fileName"];
    NSString *text = [json objectForKey:@"text"];
    NSString *textColor = [json objectForKey:@"textColor"];
    int textSize = [[json objectForKey:@"textSize"] intValue];

    NSData *nsFileData = [NSData dataWithContentsOfFile:fileName];
    UIImage *image = [[UIImage alloc] initWithData:nsFileData];

    UIFont *font = [UIFont boldSystemFontOfSize:textSize];
    UIGraphicsBeginImageContext(image.size);
    [image drawInRect:CGRectMake(0, 0,image.size.width,image.size.height)];
    CGRect rect = CGRectMake(10, image.size.height-30, image.size.width, image.size.height);
    [[self colorFromHexString:textColor] set];
    [text drawInRect:CGRectIntegral(rect) withFont:font];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    [UIImageJPEGRepresentation(newImage, 1.0) writeToFile:fileName atomically:YES];


    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:fileName];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];


}

- (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

- (void)launchNavigation:(CDVInvokedUrlCommand *)command
{
      NSDictionary *json = [command.arguments objectAtIndex:0];
      NSString *from = [json objectForKey:@"from"];
      NSString *to = [json objectForKey:@"to"];
      NSString *directionsRequest = nil;

      from = [from stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
      to = [to stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];


      NSURL *googleMapsURLScheme = [NSURL URLWithString:@"comgooglemaps-x-callback://"];
      if ([[UIApplication sharedApplication] canOpenURL:googleMapsURLScheme]) {

        NSMutableArray *params = [NSMutableArray array];
        [params addObject:[NSString stringWithFormat:@"saddr=%@", from, nil]];
        [params addObject:[NSString stringWithFormat:@"daddr=%@", to, nil]];
        if ([json objectForKey:@"travelMode"] != nil) {
          [params addObject:[NSString stringWithFormat:@"directionsmode=%@", [json objectForKey:@"travelMode"], nil]];
        }
        NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
        [params addObject:[NSString stringWithFormat:@"x-success=%@://?resume=true", bundleIdentifier, nil]];

        NSString *appName = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"];
        appName = [appName stringByReplacingOccurrencesOfString:@" " withString:@""];
        [params addObject:[NSString stringWithFormat:@"x-source=%@", appName, nil]];

        directionsRequest =
          [NSString stringWithFormat: @"comgooglemaps-x-callback://?%@", [params componentsJoinedByString: @"&"], nil];
      } else {
        directionsRequest =
          [NSString stringWithFormat: @"https://maps.apple.com/?saddr=%@&daddr=%@",
            from, to, nil];
      }
      NSURL *directionsURL = [NSURL URLWithString:directionsRequest];
      [[UIApplication sharedApplication] openURL:directionsURL];

      CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
@end

