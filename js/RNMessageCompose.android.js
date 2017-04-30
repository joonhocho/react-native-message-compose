import {NativeModules} from 'react-native';
import formatData from './formatData';


const {RNMessageCompose} = NativeModules;

export default {
  name: RNMessageCompose.name,

  send(data) {
    return RNMessageCompose.send(formatData(data));
  },
};
