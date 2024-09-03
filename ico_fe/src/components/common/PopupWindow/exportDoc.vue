<template>
  <div class="pickerOuter" ref="picker">
    <div class="bg" @click.stop="changeShowPicker"></div>
    <div class="buttons" v-show="showPicker">
      <button @click="clearPicker">取消</button>
      <button @click="exportWord" :disabled="disable">导出</button>
    </div>
    <a-range-picker
      ref="picker"
      v-if="showPicker"
      v-model="pickerValue"
      hide-trigger
      @select="onSelect"
      :disabledDate="disabledDate"
      @picker-value-change="change"
      @select-shortcut="shortcut"
      style="position: absolute; right: -8px"
    >
    </a-range-picker>
    <loading :full="true" class="loadingPosition" v-show="showLoading" />
  </div>
</template>
<script>
import dayjs from "dayjs";
import { exportWord } from "../../../assets/js/api";
import loading from "../loading.vue";
import { ElMessage } from "element-plus";
export default {
  components: { loading },
  data() {
    return {
      pickerValue: [,],
      selectedDay: null,
      rangePickerValue: null,
      showPicker: false,
      showLoading: false,
      disable: true,
      nowHour: 0,
      dayjs,
    };
  },
  watch: {
    showPicker(now, old) {
      this.$emit("showTheMask", now);
      let spans = [];
      if (now) {
        this.$nextTick(() => {
          spans = document.getElementsByClassName("arco-picker-header-title");
          if (spans[0] && spans[1]) {
            spans[0].children[1].innerText = ".";
            spans[1].children[1].innerText = ".";
            spans[0].children[1].className = "point";
            spans[1].children[1].className = "point";
          }
        });
      }
    },
  },
  created() {
    this.nowHour = dayjs().hour();
    let isSameOrAfter = require("dayjs/plugin/isSameOrAfter");
    dayjs.extend(isSameOrAfter);
  },
  methods: {
    change(value, date, dataString) {
      // console.log(value,date,dataString)
    },
    shortcut(shortcut) {
      // console.log(shortcut)
    },
    // 此函数确定范围前后两个值中被第一个选择的日期，用于禁用日期
    onSelect(valueString) {
      // console.log(valueString)
      // if(valueString.length==1){
      //   this.selectedDay = valueString[0]
      // }else if(!valueString[0]){
      //   this.selectedDay = valueString[1]
      // }
      if (
        valueString.length == 2 &&
        valueString[0] != null &&
        valueString[1] != null
      ) {
        this.disable = false;
      } else {
        this.disable = true;
      }
      if (valueString.length == 2 && valueString[0] == valueString[1]) {
        this.$nextTick(() => {
          this.pickerValue[0] = null;
          this.pickerValue[1] = null;
          this.disable = true;
        });
      }
    },
    disabledDate(current) {
      //此部分用于禁用日期,禁用今日以后和选中的第一个的左右两天
      // if(this.selectedDay){
      //     return dayjs(current).isAfter(dayjs())||Math.abs(dayjs(this.selectedDay).diff(dayjs(current),'day')) == 1
      // }else{
      if (dayjs().hour() > 2) {
        return dayjs(current).isAfter(dayjs().subtract(1, "day"));
      } else {
        // current只精确到天，dayjs()精确到时分秒，所以要限制到day相等
        return dayjs(current).isSameOrAfter(dayjs().subtract(1, "day"), "day");
      }

      // }
    },
    exportWord() {
      let params = {};
      params.start_date = this.pickerValue[0]
        ? this.pickerValue[0]
        : dayjs().subtract(7, "day").format("YYYY-MM-DD");
      params.end_date = this.pickerValue[1]
        ? this.pickerValue[1]
        : dayjs().format("YYYY-MM-DD");
      this.showLoading = true;
      exportWord(params).then(
        (res) => {
          this.download(
            res,
            `交通态势分析报告(${params.start_date}至${params.end_date})`
          );
        },
        (err) => {
          this.showLoading = false;
          if (err.response) {
            console.log(err.response);
            const reader = new FileReader();
            reader.readAsText(err.response.data, "utf-8");
            reader.onload = () => {
              const response = JSON.parse(reader.result);
              ElMessage({
                message: response.message,
                type: "error",
                customClass: "message-override",
              });
            };
          }
        }
      );
    },
    download(data, titName) {
      if (!data) {
        return;
      }
      const content = data;
      const blob = new Blob([content], {
        type: "application/msword;charset=UTF-8",
      });
      const fileName = titName ? titName : "";
      if ("download" in document.createElement("a")) {
        // 非IE下载
        const elink = document.createElement("a");
        elink.download = fileName;
        elink.style.display = "none";
        elink.href = URL.createObjectURL(blob);
        document.body.appendChild(elink);
        elink.click();
        URL.revokeObjectURL(elink.href); // 释放URL 对象
        document.body.removeChild(elink);
      } else {
        // IE10+下载
        navigator.msSaveBlob(blob, fileName);
      }
      this.showLoading = false; // 下载完成后销毁picker
      this.showPicker = false;
    },
    changeShowPicker() {
      // 点击背景图，展示就销毁，没展示就拉出
      this.pickerValue[0] = null;
      this.pickerValue[1] = null;
      this.showPicker = !this.showPicker;
      this.showLoading = false;
    },
    clearPicker() {
      this.disable = true;
      this.showPicker = false;
      this.showLoading = false;
    },
  },
};
</script>
<style>
.message-override {
  z-index: 9999 !important;
}
</style>
<style lang="less" scoped>
/deep/.arco-picker-range-container {
  top: 70px;
}
/deep/.arco-panel-year
  .arco-picker-cell:not(.arco-picker-cell-selected):not(.arco-picker-cell-range-start):not(.arco-picker-cell-range-end):not(.arco-picker-cell-disabled):not(.arco-picker-cell-week)
  .arco-picker-date-value:hover {
  background-color: rgba(64, 114, 214, 0.14);
  border-radius: 0;
}
/deep/.arco-panel-month
  .arco-picker-cell:not(.arco-picker-cell-selected):not(.arco-picker-cell-range-start):not(.arco-picker-cell-range-end):not(.arco-picker-cell-disabled):not(.arco-picker-cell-week)
  .arco-picker-date-value:hover {
  background-color: rgba(64, 114, 214, 0.14);
  border-radius: 0;
}
/deep/.arco-panel-year {
  background-color: rgba(51, 54, 73, 1);
}
/deep/.arco-panel-month {
  background-color: rgba(51, 54, 73, 1);
}
/deep/.arco-panel-year-inner {
  .arco-picker-body {
    padding-top: 20px;
  }
}
/deep/.arco-panel-month-inner {
  .arco-picker-body {
    font-family: "Noto Sans SC";
    padding-top: 20px;
  }
}
/deep/.arco-picker-cell-today:after {
  background: none;
}
/deep/.arco-picker-range-wrapper {
  user-select: none;
}
/deep/.arco-picker-date-value {
  padding: 2px 0;
  color: rgba(129, 129, 165, 1);
}
/deep/.arco-picker-cell-in-view .arco-picker-date-value {
  color: rgba(255, 255, 255, 1);
  padding: 2px 0;
}
/deep/.arco-picker-container,
.arco-picker-range-container {
  border: none;
  border-radius: 0;
}
/deep/.arco-picker-header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border-radius: 0;
}
/deep/.arco-icon {
  height: 18px;
  width: 18px;
  color: #fff;
}
/deep/.arco-picker-header-icon:not(.arco-picker-header-icon-hidden):hover {
  background-color: transparent;
}
/deep/.arco-picker-header-title {
  font-size: 16px;
  margin-top: 1px;
  color: #fff;
}
/deep/.arco-picker-header {
  box-sizing: border-box;
  height: 45px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  border-bottom: none;
  background-color: rgba(64, 114, 214, 0.14);
}
/deep/.arco-picker-week-list-item {
  color: rgba(129, 129, 165, 1);
}
/deep/.arco-picker-week-list {
  padding: 5px 20px 0 20px;
}
/deep/.arco-panel-date-inner {
  width: 250px;
  background-color: rgba(51, 54, 73, 1);
}
/deep/.arco-picker-cell-range-start .arco-picker-date {
  // border-left: 1px solid rgba(1, 255, 255, 1);
  background-color: rgbargba(4, 185, 185, 1);
  border-radius: 0;
}
/deep/.arco-picker-cell-range-end .arco-picker-date {
  // border-right: 1px solid rgba(1, 255, 255, 1);
  background-color: rgbargba(4, 185, 185, 1);
  border-radius: 0;
}
/deep/.arco-picker-body {
  padding: 0 20px 48px 20px;
}
/deep/.arco-picker-row {
  padding: 0;
}
/deep/.arco-panel-date {
  width: 250px;
}
/deep/.arco-picker-cell .arco-picker-date {
  padding: 0;
  position: relative;
}
/deep/.arco-picker-date-value,
.arco-picker-cell-range-start .arco-picker-date-value,
.arco-picker-cell-range-end .arco-picker-date-value {
  border-radius: 0;
}
/deep/.arco-picker-cell-in-view:not(.arco-picker-cell-selected):not(.arco-picker-cell-range-start):not(.arco-picker-cell-range-end):not(.arco-picker-cell-disabled):not(.arco-picker-cell-week)
  .arco-picker-date-value:hover {
  color: #fff;
  width: 100%;
  background-color: rgba(86, 117, 145, 1);
}

/deep/.arco-picker-cell-in-range .arco-picker-date .arco-picker-date-value {
  color: rgba(4, 185, 185, 1) !important;
}
/deep/.arco-picker-cell-in-range .arco-picker-date {
  background-color: rgba(1, 255, 255, 0.15);
}
/deep/.arco-picker-row .arco-picker-cell-range-end .arco-picker-date-value {
  position: absolute;
  top: 0;
  width: 100%;
  background-color: rgba(4, 185, 185, 1);
  color: #fff !important;
}
/deep/.arco-picker-row .arco-picker-cell-range-start .arco-picker-date-value {
  position: absolute;
  top: 0;
  width: 100%;
  background-color: rgba(4, 185, 185, 1);
  color: #fff !important;
}
/deep/.arco-picker-cell-disabled .arco-picker-date {
  background-color: transparent;
}
/deep/.arco-picker-cell-disabled .arco-picker-date-value {
  padding: 2px 0;
  color: #8181a5;
}
.pickerOuter {
  position: relative;
}
.bg {
  position: relative;
  left: 1px;
  width: 30px;
  height: 30px;
  background: url("../../../assets/img/Frame 3565.png");
  background-repeat: no-repeat;
  background-size: 100% 100%;
  box-shadow: none;
  &:hover {
    cursor: pointer;
  }
}
.buttons {
  user-select: none;
  position: absolute;
  display: flex;
  justify-content: space-between;
  right: 18px;
  top: 334px;
  width: 148px;
  height: 25px;
  font-size: 14px;
  z-index: 1;

  button {
    border: none;
    width: 65px;
    font-family: "Noto Sans SC";
    color: #fff;
    background-color: #409eff;
    border-radius: 4px;
    &:hover {
      background-color: #79bbff;
      cursor: pointer;
    }
  }
  button:nth-child(1) {
    border: 1px solid #79bbff;
    background-color: rgba(0, 0, 51, 1);
  }
  button:nth-child(2) {
    &:disabled {
      background-color: rgba(172, 190, 255, 1);
      user-select: none;
      cursor: not-allowed;
    }
  }
}
.buttons::after {
  content: "";
  position: absolute;
  top: -10px;
  right: 0;
  height: 1px;
  width: 452px;
  background-color: rgba(74, 78, 104, 1);
}
.loadingPosition {
  // 无法获取日历节点使Loading成为其子元素，因此直接罩上去。
  width: 500px;
  height: 320px;
  top: 50px;
  right: -8px;
}
/deep/.point {
  font-size: 24px;
  display: inline-block;
  width: 5px;
}
</style>
