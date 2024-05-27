#import <React/RCTViewManager.h>
#import <RiveRuntime/RiveRuntime-Swift.h>

@interface RCT_EXTERN_MODULE(RiveViewManager, RCTViewManager)
RCT_EXPORT_VIEW_PROPERTY(resourceName, NSString)
@end

@implementation RiveViewManager

RCT_EXPORT_MODULE()

- (UIView *)view {
  return [[RiveView alloc] init];
}

@end