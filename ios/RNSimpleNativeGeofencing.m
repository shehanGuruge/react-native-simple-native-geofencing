

#import <Foundation/Foundation.h>


#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>


@interface RCT_EXTERN_MODULE(RNSimpleNativeGeofencing, RCTEventEmitter)


RCT_EXTERN_METHOD(initNotification: (NSDictionary *)settings)

RCT_EXTERN_METHOD(addGeofence:  (NSDictionary *)geofence
                  duration:(NSInteger *)duration)

RCT_EXTERN_METHOD(addGeofences: (NSArray *)geofencesArray
                  duration:(NSInteger *)duration
                  failCallback:(RCTResponseSenderBlock *)failCallback)

RCT_EXTERN_METHOD(updateGeofences:  (NSArray *)geofencesArray
                  duration:(NSInteger *)duration)

RCT_EXTERN_METHOD(addMonitoringBorder:  (NSDictionary *)geofence
                  duration:(NSInteger *)duration)

RCT_EXTERN_METHOD(removeMonitoringBorder)

RCT_EXTERN_METHOD(removeAllGeofences)

RCT_EXTERN_METHOD(removeGeofence: (NSString *)geofenceKey)

RCT_EXTERN_METHOD(startMonitoring)

RCT_EXTERN_METHOD(stopMonitoring)

@end
