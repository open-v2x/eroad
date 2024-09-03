<template>
  <div class="dict-data-page">
    <el-breadcrumb>
      <el-breadcrumb-item :to="{ path: '/admin/system/dict' }">字典管理</el-breadcrumb-item>
      <el-breadcrumb-item>字典数据</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never" class="search-card">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" ref="formRef">
        <el-form-item label="字典名称" prop="deviceType">
          <el-input v-model="searchForm.deviceType" placeholder="字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="manufacturer">
          <el-select v-model="searchForm.manufacturer" placeholder="字典编码">
            <el-option label="Zone one" value="shanghai" />
            <el-option label="Zone two" value="beijing" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="search">查 询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 按钮操作区 -->
       <div style="display: flex; justify-content: space-between; align-items: center;">
        <el-button @click="goBack">返 回</el-button>
        <el-button type="primary" @click="handleAdd">新 增</el-button>
       </div>
      <!-- 设备列表 -->
      <el-table :data="tableData" border>
        <el-table-column prop="dictName" label="字典名称" align="center"/>
        <el-table-column prop="dictEncoding" label="字典编码" align="center"/>
        <el-table-column prop="remarks" label="备注" align="center"/>
        <el-table-column prop="createTime" label="创建时间" align="center"/>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button text size="small" type="primary" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
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
  <DictDataModal
    v-if="isModalVisible"
    :currentData="currentData"
    @close="isModalVisible = false"
    @success="fetchDataList"
  />
</template>

<script setup>
import DictDataModal from './Components/DictDataModal.vue'
import { useRouter, useRoute } from 'vue-router';
import { ref, reactive, onMounted } from 'vue'
import { systemDictList, systemDictDelete } from '../../Service/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0) // 用于记录总条目数
const formRef = ref()
const isModalVisible = ref(false)
const currentData = ref({})
const router = useRouter();
const route = useRoute();

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const goBack = () => {
  router.go(-1)
}

const handleAdd = () => {
  currentData.value = {}
  isModalVisible.value = true
}

const handleEdit = (index, row) => {
  currentData.value = row
  isModalVisible.value = true
}

const fetchDataList = async () => {
  try {
    const pid = route.params.id;
    const res = await systemDictList({...searchForm, pid})
    if (res.body.list) {
      tableData.value = res.body.list
      total.value = body.totalCount
    }
  } catch (error) {
    console.log(error);
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

const handleDelete = (index, row) => {
  ElMessageBox.confirm(
    '确定要删除吗?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await systemDictDelete(row)
        ElMessage({
          type: 'success',
          message: '删除成功',
        })
        fetchDataList()
      }catch(err) {
        console.log(err);
      }
    })
    .catch(() => {})}

const handleSizeChange = (val) => {
  searchForm.pageSize = val
  fetchDataList() // 当页面大小变化时重新获取数据
}

const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchDataList() // 当当前页码变化时重新获取数据
}

onMounted(() => {
  fetchDataList()
})
</script>

<style lang="less">
.dict-data-page {
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
