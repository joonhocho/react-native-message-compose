//
//  RNMessageCompose.swift
//  DropCard
//
//  Created by Joon Ho Cho on 4/30/17.
//

import Foundation
import MobileCoreServices
import MessageUI


@objc(RNMessageCompose)
class RNMessageCompose: NSObject, MFMessageComposeViewControllerDelegate {
  var resolve: RCTPromiseResolveBlock?
  var reject: RCTPromiseRejectBlock?
  
  @objc func constantsToExport() -> [String: Any] {
    return [
      "name": "RNMessageCompose",
    ]
  }
  
  @objc func canSendText(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    return resolve(MFMessageComposeViewController.canSendText())
  }
  
  @objc func canSendAttachments(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    return resolve(MFMessageComposeViewController.canSendAttachments())
  }
  
  @objc func canSendSubject(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    return resolve(MFMessageComposeViewController.canSendSubject())
  }
  
  func mimeToUti(mimeType: String?) -> String? {
    if let mimeType = mimeType, let type = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType, mimeType as CFString, nil) {
      return type.takeRetainedValue() as String
    }
    return nil
  }
  
  func textToData(utf8: String?, base64: String?) -> Data? {
    if let utf8 = utf8 {
      return utf8.data(using: .utf8)
    }
    if let base64 = base64 {
      return Data(base64Encoded: base64, options: .ignoreUnknownCharacters)
    }
    return nil
  }
  
  func toFilename(filename: String?, ext: String?) -> String? {
    if let ext = ext {
      return (filename ?? UUID().uuidString) + ext
    }
    return nil
  }
  
  @objc func send(_ data: [String: Any], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    if !MFMessageComposeViewController.canSendText() {
      reject("cannotSendText", "Cannot send text", nil)
      return
    }
    
    let vc = MFMessageComposeViewController()
    
    if let value = data["recipients"] as? [String] {
      vc.recipients = value
    }
    if let value = data["body"] as? String {
      vc.body = value
    }
    if MFMessageComposeViewController.canSendSubject(), let value = data["subject"] as? String {
      vc.subject = value
    }
    if MFMessageComposeViewController.canSendAttachments(), let value = data["attachments"] as? [[String: String]] {
      for dict in value {
        if let data = textToData(utf8: dict["text"], base64: dict["data"]), let uti = mimeToUti(mimeType: dict["mimeType"]), let filename = toFilename(filename: dict["filename"], ext: dict["ext"]) {
          vc.addAttachmentData(data, typeIdentifier: uti, filename: filename)
        }
      }
    }
    
    vc.messageComposeDelegate = self
    
    self.resolve = resolve
    self.reject = reject
    
    UIApplication.shared.keyWindow?.rootViewController?.present(vc, animated: true, completion: nil)
  }
  
  func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
    switch (result) {
    case .cancelled:
      reject?("cancelled", "Operation has been cancelled", nil)
      break
    case .sent:
      resolve?("sent")
      break
    case .failed:
      reject?("failed", "Operation has failed", nil)
      break
    }
    resolve = nil
    reject = nil
    
    controller.dismiss(animated: true, completion: nil)
  }
}
