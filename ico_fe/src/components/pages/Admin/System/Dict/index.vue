<template>
  <div class="dict-page">
    <el-breadcrumb>
      <el-breadcrumb-item>字典管理</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never" class="search-card">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" ref="formRef">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="searchForm.dictName" placeholder="字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictEncoding">
          <el-input v-model="searchForm.dictEncoding" placeholder="字典编码" />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="search">查 询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 按钮操作区 -->
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <!-- 设备列表 -->
      <el-table :data="tableData" border>
        <el-table-column prop="dictName" label="字典名称" align="center"/>
        <el-table-column porp="dictEncoding" label="字典编码" align="center" :show-overflow-tooltip="true">
          <template #default="scope">
            <router-link :to="'/admin/system/dict/' + scope.row.id" class="link-type">
              <span>{{ scope.row.dictEncoding }}</span>
            </router-link>
          </template>
        </el-table-column>
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
    <DictModal
      v-if="isModalVisible"
      :currentData="currentData"
      @close="isModalVisible = false"
      @success="fetchDataList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { systemDictDelete, systemDictFind } from '../../Service/api'
import DictModal from './Components/DictModal.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0) // 用于记录总条目数
const formRef = ref()
const isModalVisible = ref(false)
const currentData = ref({})

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const handleAdd = () => {
  currentData.value = {};
  isModalVisible.value = true
}

const handleEdit = (index, row) => {
  currentData.value = row;
  isModalVisible.value = true
}

const fetchDataList = async () => {
  try {
    const { body } = await systemDictFind(searchForm)
    if (body) {
      tableData.value = body // 更新 tableData 数据
      total.value = body.length // 更新总条目数
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
    .catch(() => {})
}

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
.dict-page {
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
