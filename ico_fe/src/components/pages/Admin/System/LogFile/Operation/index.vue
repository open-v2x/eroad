<template>
  <div class="operation-page">
    <el-breadcrumb>
      <el-breadcrumb-item>操作日志  </el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never" class="search-card">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" ref="formRef">
        <el-form-item label="操作人" prop="creator">
          <el-input v-model="searchForm.creator" placeholder="操作人" />
        </el-form-item>
        <el-form-item label="操作内容" prop="operationAction">
          <el-input v-model="searchForm.operationAction" placeholder="操作内容" />
        </el-form-item>
        <el-form-item label="操作对象" prop="operationObject">
          <el-input v-model="searchForm.operationObject" placeholder="操作对象" />
        </el-form-item>
        <el-form-item label="起止时间" prop="alertStatus">
          <el-date-picker
            v-model="time"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="search">查 询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 按钮操作区 -->
      <el-button type="primary" @click="fetchDeviceAlarmExpire">清楚超时日志</el-button>

      <!-- 设备列表 -->
      <el-table :data="tableData" border>
        <!-- <el-table-column prop="date" label="应用编号" width="180"/> -->
        <el-table-column prop="creator" label="操作人" width="180"/>
        <!-- <el-table-column prop="address" label="创建人ID"/> -->
        <el-table-column prop="operationAction" label="操作内容"/>
        <el-table-column prop="operationObject" label="操作对象"/>
        <el-table-column prop="id" label="主键"/>
        <el-table-column prop="params" label="操作参数"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
            <el-tag v-else type="danger">已删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gmtCreated" label="创建时间"/>
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
import { systemLogList, deviceAlarmExpire } from '../../../Service/api'
import { ElMessage } from 'element-plus'
import moment from 'moment'

const tableData = ref([{id: 1}])
const total = ref(0) // 用于记录总条目数
const formRef = ref()
const time = ref([])

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})


const fetchDataList = async () => {
  try {
    const beginTime = time.value[0] ? moment(time.value[0]).format('YYYY-MM-DD HH:mm:ss') : null
    const endTime = time.value[1] ? moment(time.value[1]).format('YYYY-MM-DD HH:mm:ss') : null
    const { body } = await systemLogList({
      ...searchForm,
      beginTime,
      endTime,
      sorter: {
        "field": "gmt_created",
        "order": "descend"
      }
    })
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

const fetchDeviceAlarmExpire = async () => {
  const res = await deviceAlarmExpire()
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
.operation-page {
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
