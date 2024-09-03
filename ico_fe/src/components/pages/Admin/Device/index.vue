<template>
  <div class="device-page">
    <el-breadcrumb>
      <el-breadcrumb-item>设备管理</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never" class="search-card">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" ref="formRef">
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="searchForm.deviceType" placeholder="设备厂商">
            <el-option
              v-for="item in deviceTypeOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备厂商" prop="manufacturer">
          <el-select v-model="searchForm.manufacturer" placeholder="设备厂商">
            <el-option
              v-for="item in manufacturerOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="在线状态" prop="onlineState">
          <el-select v-model="searchForm.onlineState" placeholder="在线状态">
            <el-option label="在线" :value="0" />
            <el-option label="离线" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警状态" prop="alarmState">
          <el-select v-model="searchForm.alarmState" placeholder="告警状态">
            <el-option label="正常" :value="0" />
            <el-option label="告警" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="searchForm.deviceCode" placeholder="设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="searchForm.deviceName" placeholder="设备名称" />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="search">查 询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 按钮操作区 -->
      <el-button type="primary" @click="handleAdd">新 增</el-button>
      <el-button type="primary" @click="handleBatchDelete">批量删除</el-button>
      <!-- <el-upload
        v-model:file-list="fileList"
        :show-file-list="false"
        class="upload-demo"
        action=""
        multiple
      >
        <el-button type="primary">批量导入</el-button>
      </el-upload>
      <el-button type="primary" @click="handleBatchExport">批量导出</el-button> -->
      <!-- 设备列表 -->
      <el-table :data="tableData" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="deviceId" label="设备编码" width="180"/>
        <el-table-column prop="deviceName" label="设备名称" width="180"/>
        <el-table-column prop="deviceType" label="设备类型">
          <template #default="slot">
            <span>
              {{ deviceTypeOptions.find(item => item.value === slot.row.deviceType).label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="设备厂商">
          <template #default="slot">
            <span>
              {{ manufacturerOptions.find(item => item.value === slot.row.manufacturer).label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="onlineState" label="在线状态">
          <template #default="slot">
            <el-tag :type="getTagType(slot.row.alarmState)">
              {{ slot.row.alarmState === 0 ? '在线' : '离线'}}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmState" label="告警状态">
          <template #default="slot">
            <el-tag :type="getTagType(slot.row.alarmState)">
              {{ slot.row.alarmState === 0 ? '正常' : '告警'}}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="networkLink" label="网络状态">
          <template #default="slot">
            <span>{{ slot.row.networkLink === 0 ? '无网络' : '有网络'}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button text size="small" type="primary" @click="handleView(scope.$index, scope.row)">详情</el-button>
            <el-button text size="small" type="primary" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button text size="small" type="primary" @click="handleRest(scope.$index, scope.row)">重启</el-button>
            <el-button text size="small" type="primary" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
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

  <DeviceModal
    v-if="isModalVisible"
    @success="successDeviceModal"
    @close="isModalVisible = false"
    :currentData="currentData"
    :currentType="currentType"
    :deviceTypeOptions="deviceTypeOptions"
    :manufacturerOptions="manufacturerOptions"
  />
</template>

<script setup>
import DeviceModal from './Components/DeviceModal.vue'
import { ref, reactive, onMounted } from 'vue'
import { getDeviceList, systemrpcFind, deviceDeleteList, deviceExcelImport, deviceExcelExport } from '../Service/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0) // 用于记录总条目数
const formRef = ref()
const isModalVisible = ref(false)
const currentData = ref({})
const currentType = ref('add')
const multipleSelection = ref([])
const manufacturerOptions = ref([])
const deviceTypeOptions = ref([])
const fileList = ref([])

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const getTagType = (alarmState) => {
  return alarmState === 0 ? 'success' : 'danger';
}
const handleSelectionChange = (val) => {
  console.log(val);
  multipleSelection.value = val;
}

const handleAdd = () => {
  currentData.value = {}
  currentType.value = 'add'
  isModalVisible.value = true
}

const successDeviceModal = () => {
  isModalVisible.value = false
  fetchDataList() // 重新获取数据
}

const handleView = (index, row) => {
  currentData.value = row
  currentType.value = 'view'
  isModalVisible.value = true
}

const handleEdit = (index, row) => {
  currentData.value = row
  currentType.value = 'edit'
  isModalVisible.value = true
}

const handleRest = async () => {
  const conform = await ElMessageBox.confirm(
    '确认要重启吗？',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: '提示',
    }
  )
  if (!conform) return
  ElMessage({
    message: '重启成功',
    type: 'success',
  })
}

const fetchDataList = async () => {
  try {
    const { body } = await getDeviceList(searchForm)
    tableData.value = body.list
    total.value = body.totalCount // 更新总条目数
  } catch (error) {
    console.log(error);
  }
}
const fetchSystemData = async (data) => {
  try {
    const res = await systemrpcFind(data);
    return res.body.map((item) => {
      return {
        ...item,
        label: item.dictName,
        value: item.dictEncoding
      }
    })
  } catch (error) {
    console.error('Error fetching system data:', error); // 处理错误情况
  }
}


const search = () => {
  searchForm.pageNum = 1 // 查询时重置页码
  fetchDataList()
}


const resetForm = () => {
  searchForm.pageNum = 1
  searchForm.pageSize = 10
  formRef.value.resetFields()
  fetchDataList() // 重置后重新获取数据
}

const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('没有选中任何设备，请重新选择');
    return false;
  }

  ElMessageBox.confirm(
    '确认要批量删除吗？',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: '提示',
    }
  )
  .then(async () => {
    const {head} = await deviceDeleteList({deviceId: multipleSelection.value.map(item => item.deviceId)})
    if (head.code === '000000') {
      ElMessage.success('删除成功')
      fetchDataList()
    } else {
      ElMessage.error('删除失败')
    }
  })
  .catch(() => {})
}

const handleDelete = async (index, row) => {
  const conform = await ElMessageBox.confirm(
    '确认要删除吗？',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: '提示',
    }
  )
  if (!conform) return
  const {head} = await deviceDeleteList({deviceId: [row.deviceId]})
  if (head.code === '000000') {
    ElMessage.success('删除成功')
    fetchDataList()
  } else {
    ElMessage.error('删除失败')
  }

}

const handleBatchExport = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('没有选中任何设备，请重新选择');
    return false;
  }
  try {
    const res = await deviceExcelExport(multipleSelection.value)
    if (!res) {
      throw new Error('Network res was not ok');
    }
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = '设备列表.xlsx';
    document.body.appendChild(a);
    a.click();
    // 移除a标签
    document.body.removeChild(a);
    // 释放Object URL，防止内存泄漏
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error('Error exporting data:', err);
  }
}

const handleBatchImport = async () => {

}
const downloadFile = (blobData, fileName) => {
  // 创建下载链接
  const url = URL.createObjectURL(blobData);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);

  // 触发点击事件进行下载
  link.click();

  // 清理
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};
const handleSizeChange = (val) => {
  searchForm.pageSize = val
  fetchDataList() // 当页面大小变化时重新获取数据
}

const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchDataList() // 当当前页码变化时重新获取数据
}

onMounted(async () => {
  fetchDataList()
  manufacturerOptions.value = await fetchSystemData({ dictEncoding: 'manufacturer' })
  deviceTypeOptions.value = await fetchSystemData({ dictEncoding: 'deviceType' })
})
</script>

<style lang="less">
.el-form--inline {
  .el-form-item {
    & > .el-input, .el-cascader, .el-select, .el-date-editor, .el-autocomplete {
      width: 9vw;
    }
  }
}
.device-page {
  .el-breadcrumb {
    margin-bottom: 20px;
  }
  .search-card {
    margin-bottom: 20px;
    .el-card__body {
      padding-bottom: 0;
    }
  }

  .action {
    display: flex;
    justify-content: flex-end;
    .upload-demo {
      margin: 0 10px;
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
