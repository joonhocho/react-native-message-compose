import {NativeModules} from 'react-native';
import formatData from './formatData';


const {RNMessageCompose} = NativeModules;

export default {
  name: RNMessageCompose.name,

  canSendText() {
    return RNMessageCompose.canSendText();
  },

  canSendAttachments() {
    return RNMessageCompose.canSendAttachments();
  },

  canSendSubject() {
    return RNMessageCompose.canSendSubject();
  },

  send(data) {
    return RNMessageCompose.send(formatData(data));
  },
};
