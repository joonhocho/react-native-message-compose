//
//  RNMessageComposeBridge.m
//  DropCard
//
//  Created by Joon Ho Cho on 4/30/17.
//

#import <Foundation/Foundation.h>
#import <Messages/Messages.h>
#import <MessageUI/MessageUI.h>

#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(RNMessageCompose, NSObject)

RCT_EXTERN_METHOD(canSendText:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(canSendAttachments:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(canSendSubject:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(send:(NSDictionary *)data
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

@end
