<template>
  <el-dialog 
    v-model="props.visible" 
    :title="!!id ? '字典新增' : '字典编辑'"
    :before-close="closeModal"
    width="35%"
   >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="dynamicLabelWidth"
      status-icon
    >
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="form.dictName" />
      </el-form-item>
      <el-form-item label="字典编码" prop="dictEncoding">
        <el-input v-model="form.dictEncoding" :disabled="!!id"/>
      </el-form-item>
      <el-form-item label="备注" prop="remarks">
        <el-input v-model="form.remarks" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeModal">取 消</el-button>
        <el-button type="primary" @click="saveData">确 认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { systemDictAdd, systemDictUpdate } from '../../../Service/api'
import { getDynamicLabelWidth } from '@/utils/utils.js';
const props = defineProps({
  visible: {
    type: Boolean,
    default: true,
  },
  onClose: {
    type: Function,
    default: () => {}
  },
  currentData: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['close', 'success'])

const dynamicLabelWidth = computed(() => getDynamicLabelWidth(100))

const id = ref()
const formRef = ref(null)

const form = ref({
  dictName: '',
  dictEncoding: '',
  remarks: '',
  pid: 0,
})

const rules = {
  dictName: [
    {
      required: true,
      message: '请输入字典名称',
      trigger: 'blur',
    },
  ],
  dictEncoding: [
    {
      required: true,
      message: '请输入字典类型',
      trigger: 'blur',
    },
  ],
}

const closeModal = () => {
  emit('close')
}

const saveData = async () => {
  const validate = await formRef.value.validate()
  if (!validate) return
  const fetch = id.value ? systemDictUpdate : systemDictAdd;
  const res = await fetch(form.value)
  if (res) {
    emit('success')
    closeModal() // 关闭弹窗
  }
}

onMounted(() => {
  if (props.currentData.id) {
    const data = JSON.parse(JSON.stringify(props.currentData))
    form.value = data
    id.value = data.id
  }
})
</script>

<style scoped>
</style>
