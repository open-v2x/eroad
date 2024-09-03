
import { initGlobalState } from "qiankun";

const initialState = { msg: 0 }; // 全局状态池给了个默认值
const shareActions = initGlobalState(initialState);

export default shareActions;
