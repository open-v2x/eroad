<template>
  <div class="strandardOuter">
    <div v-if="standardType && !onlyButton" class="type">
      <span><slot name="left"></slot></span>
      <el-select
        v-model="value"
        class="input1"
        filterable
        placeholder="请选择路段"
      >
        <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </div>
    <div v-if="!onlyButton" class="calendar">
      <span><slot name="right"></slot></span>
      <el-date-picker
        v-model="dateList"
        type="daterange"
        :disabled="timeQuantumActive"
        :disabled-date="disabledDate"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="截止时间"
        class="input3"
      />
    </div>
    <div class="button active click" @click="generate" ref="click">生成</div>
  </div>
</template>

<script>
import dayjs from "dayjs";

export default {
  props: {
    standardType: {
      type: Boolean,
      default: true,
    },
    onlyButton: {
      type: Boolean,
      default: false,
    },
    options: {
      type: Array,
      default() {
        return [{ value: "total", label: "全部路段" }];
      },
    },
    timeQuantumActive: {
      type: Boolean,
    },
  },
  data() {
    return {
      value: "total",
      dateList: [dayjs().subtract(7, "day"), dayjs().subtract(1, "day")], // 默认显示七日
    };
  },
  watch: {
    options: {
      handler() {
        this.value = this.options[0].value;
      },
      immediate: true,
    },
    value: 'disabled',
    dateList: 'disabled',
    timeQuantumActive: 'disabled'
  },
  methods: {
    disabledDate(time) {
      return (
        dayjs(time).isAfter(dayjs().subtract(1, "day")) ||
        dayjs(time).isBefore(dayjs().subtract(1, "month").subtract(1, "day"))
      );
    },
    generate() {
      if (this.timeQuantumActive ? this.value : this.value && this.dateList) {
        this.$emit("generate", {
          roadSection: this.value,
          date: this.dateList, // 日期用到，时段不需要但仍然传。
          timeQuantumActive: this.timeQuantumActive, // 时段是true，日期是False
        });
      }
    },
    disabled() {
      if (this.timeQuantumActive ? this.value : this.value && this.dateList) {
        this.$refs.click.classList.remove("disabled");
      } else {
        this.$refs.click.classList.add("disabled");
      }
    }
  },
  mounted() {
    this.disabled();
    this.generate();
  }
};
</script>

<style lang="less" scoped>
input {
  display: inline-block;
  background: none;
  outline: none;
  border: 2px solid rgba(51, 153, 255, 0.8);
  border-radius: 4px;
  color: #fff;
  box-sizing: border-box;
  padding-left: 8px;
  height: 32px;
}

.strandardOuter {
  height: 32px;
  display: flex;
  align-items: center;
  font-family: "Noto Sans SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
  .calendar {
    margin-right: 32px;
  }
  > div {
    display: flex;
    align-items: center;
    // flex: 1 0 auto;
    margin-right: 16px;
    span {
      color: rgba(51, 153, 255, 0.8);
      margin-right: 6px;
    }
  }
}
.type {
  display: flex;
  width: 204px;
}

.button {
  text-align: center;
  user-select: none;
  justify-content: center;
  height: 30px;
  width: 80px;
  border-radius: 2px;
  cursor: pointer;
  &:hover {
    background-color: #79bbff;
  }
  &:active {
    background-color: #337ecc;
  }

  margin-right: 8px;
}
.disabled {
  cursor: not-allowed;
}
.active {
  background-color: #3399ff;
  color: #fff;
  font-weight: 700;
}

.input1 {
  width: 140px;
}
.input2 {
  width: 160px;
}
.input3 {
  width: 240px !important;
}
:deep(.el-range-editor.el-input__wrapper) {
  width: 240px;
}
:deep(.el-range-editor.is-disabled){
  background-color: transparent ;
} 
:deep(.el-range-editor.is-disabled input){
  background-color: transparent ;
}
:deep(.el-date-editor .el-range-separator){
  color: #fff;
}
</style>
