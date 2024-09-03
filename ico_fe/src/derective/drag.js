export const drag = (app) => {
  app.directive('drag',{
    mounted(el){
      let moveEl = el.firstElementChild;
      moveEl.style.cursor = 'move'
      const mouseDown = (e) => {
        let X = e.clientX - el.offsetLeft;
        let Y = e.clientY - el.offsetTop;
        const move = (e) => {
          el.style.left = e.clientX - X + "px";
          el.style.top = e.clientY - Y + "px";
        };
        document.addEventListener("mousemove", move);
        document.addEventListener("mouseup", () => {
          document.removeEventListener("mousemove", move);
        });
      };
      moveEl.addEventListener("mousedown", mouseDown);
    }
  })
}
