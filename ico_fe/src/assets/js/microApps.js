import actions from "./qiankunActions";

qiankunApps.forEach((item)=>{
  item.props.actions = actions
})
console.log('qiankunApps----------------------->',qiankunApps)
export default qiankunApps
