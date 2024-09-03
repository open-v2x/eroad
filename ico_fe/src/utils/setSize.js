export const setSize = s => {
  let w = document.body.clientWidth   //网页可见区域宽
  return  s * w / 1920
}