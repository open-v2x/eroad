<template>
  <div class="alarm-page">
    <el-breadcrumb>
      <el-breadcrumb-item>告警日志</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never" class="search-card">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" ref="formRef">
        <el-form-item label="告警级别" prop="alarmLevel">
          <el-input v-model="searchForm.alarmLevel" placeholder="告警级别" />
        </el-form-item>
        <el-form-item label="告警状态" prop="alarmStatus">
          <el-input v-model="searchForm.alarmStatus" placeholder="告警状态" />
        </el-form-item>
        <el-form-item label="告警类型" prop="alarmType">
          <el-input v-model="searchForm.alarmType" placeholder="告警类型" />
        </el-form-item>
        <el-form-item label="设备SN码" prop="sn">
          <el-input v-model="searchForm.sn" placeholder="设备SN码" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-input v-model="searchForm.deviceType" placeholder="设备类型" />
        </el-form-item>
        <el-form-item label="起止时间" prop="time">
          <el-date-picker v-model="time" type="daterange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="search">查 询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 按钮操作区 -->
      <el-button type="primary" @click="fetchSystemLogDel">清楚超时日志</el-button>

      <!-- 设备列表 -->
      <el-table :data="tableData" border>
        <el-table-column prop="date" label="ID" width="180"/>
        <el-table-column prop="sn" label="设备SN码" width="180"/>
        <el-table-column prop="address" label="设备类型"/>
        <el-table-column prop="manufacturer" label="告警状态"/>
        <el-table-column prop="alertStatus" label="告警类型"/>
        <el-table-column prop="networkStatus" label="简短描述"/>
        <el-table-column prop="failCount" label="告警级别"/>
        <el-table-column prop="failCount" label="告警时间"/>
        <el-table-column prop="failCount" label="上报时间"/>
        <el-table-column prop="failCount" label="创建时间"/>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button text size="small" type="primary" @click="handleView(scope.$index, scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :page-sizes="[10, 20, 50, 100, 200]"
        layout="total, sizes, prev, pager, next, jumper"
        size="small"
        background
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { deviceAlarmPage, systemLogDel } from '../../../Service/api'
import { ElMessage } from 'element-plus'
import moment from 'moment'

const tableData = ref([])
const total = ref(0) // 用于记录总条目数
const formRef = ref()
const isModalVisible = ref(false)
const time = ref([])

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})


const handleView = () => {
  isModalVisible.value = true
}

const fetchDataList = async () => {
  try {
    const beginTime = time.value[0] ? moment(time.value[0]).format('YYYY-MM-DD') : ''
    const endTime = time.value[1] ? moment(time.value[1]).format('YYYY-MM-DD') : ''
    const { body } = await deviceAlarmPage({...searchForm, beginTime, endTime})
    tableData.value = body.list
    total.value = body.totalCount
  } catch (error) {
    console.log(error);
  }
}


const search = () => {
  searchForm.pageNum = 1 // 查询时重置页码
  fetchDataList()
}


const resetForm = () => {
  time.value = []
  searchForm.pageNum = 1
  searchForm.pageSize = 10
  formRef.value.resetFields()
  fetchDataList() // 重置后重新获取数据
}

const handleSizeChange = (val) => {
  searchForm.pageSize = val
  fetchDataList() // 当页面大小变化时重新获取数据
}

const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchDataList() // 当当前页码变化时重新获取数据
}

const fetchSystemLogDel = async () => {
  const res = await systemLogDel()
  console.log(res, '清楚超时日志');
  if(res.body) {
    ElMessage({
      type: 'success',
      message: res.body || '操作成功'
    })
  }
}

onMounted(() => {
  fetchDataList()
})
</script>

<style lang="less">
.alarm-page {
  .el-breadcrumb {
    margin-bottom: 20px;
  }
  .search-card {
    margin-bottom: 20px;
    .el-card__body {
      padding-bottom: 0;
    }
  }

  .table-card {
    .el-card__body {
      text-align: right;
      .el-table {
        margin-top: 20px;
      }
      .el-pagination {
        margin-top: 20px;
      }
    }
  }
}
</style>
