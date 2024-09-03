<template>
  <el-dialog
    v-model="props.visible"
    :title="currentData.deviceId ? '设备编辑' : '设备新增'"
    :before-close="closeModal"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="demo-form"
      status-icon
      :disabled="currentType === 'view'"
    >
      <div class="form-row">
        <el-form-item label="设备编码" prop="deviceId">
          <el-input v-model="form.deviceId" :disabled="currentType === 'view' || currentType === 'edit'"/>
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="设备厂商">
            <el-option
              v-for="item in deviceTypeOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="厂商" prop="manufacturer">
          <el-select v-model="form.manufacturer" placeholder="设备厂商">
            <el-option
              v-for="item in manufacturerOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="网络类型" prop="networkType">
          <el-select v-model="form.networkType" placeholder="属地">
            <el-option
              v-for="item in networkTypeOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父设备编码" prop="parentDeviceId">
          <el-input v-model="form.parentDeviceId" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="属地" prop="dependency">
          <el-select v-model="form.dependency" placeholder="属地">
            <el-option
              v-for="item in dependencyOptions"
              :key="item.id"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备区划" prop="deviceArea">
          <el-input v-model="form.deviceArea" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="端口" prop="port">
          <el-input v-model="form.port" />
        </el-form-item>
        <el-form-item label="设备IP" prop="deviceIp">
          <el-input v-model="form.deviceIp" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="服务IP或域名" prop="domainName">
          <el-input v-model="form.domainName" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model="form.longitude" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="form.latitude" />
        </el-form-item>
        <el-form-item label="高程" prop="altitude">
          <el-input v-model="form.altitude" />
        </el-form-item>
      </div>
      <div class="form-row">
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" />
        </el-form-item>
        <el-form-item label="备注" prop="comment">
          <el-input v-model="form.comment" />
        </el-form-item>
      </div>
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
import { ref, onMounted } from 'vue'
import { systemrpcFind, deviceAddList, deviceEditList } from '../../Service/api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: true,
  },
  currentData: {
    type: Object,
    default: () => ({}),
  },
  currentType: {
    type: String,
    default: ''
  },
  onClose: Function,
  deviceTypeOptions: {
    type: Array,
    default: () => [],
  },
  manufacturerOptions: {
    type: Array,
    default: () => [],
  }
})

const emit = defineEmits(['close', 'success'])

const formRef = ref(null)
const dependencyOptions = ref([])
const networkTypeOptions = ref([])
const form = ref({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  manufacturer: '',
  networkType: '',
  parentDeviceId: '',
  dependency: '',
  deviceArea: '',
  port: '',
  deviceIp: '',
  domainName: '',
  longitude: '',
  latitude: '',
  altitude: '',
  version: '',
  comment: '',
})

const rules = {
  deviceId: [
    {
      required: true,
      message: '请输入设备编码',
      trigger: 'blur',
    },
  ],
  deviceName: [
    {
      required: true,
      message: '请输入设备名称',
      trigger: 'blur',
    },
  ],
  deviceType: [
    {
      required: true,
      message: '请选择设备类型',
      trigger: 'change',
    },
  ],
  manufacturer: [
    {
      required: true,
      message: '请选择厂商',
      trigger: 'change',
    },
  ],
}

const closeModal = () => {
  emit('close')
}

const saveData = async () => {
  try {
    const validate = await formRef.value.validate()
    if (!validate) return
    const fetch = props.currentData.deviceId ? deviceEditList : deviceAddList;
    const { head } = await fetch(form.value)
    if (head.code === '000000') {
      emit('success')
      closeModal() // 关闭弹窗
    }else {
      ElMessage.error(head.msg || '保存失败')
    }
  } catch (err) {
    console.log(err, 'err');
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

onMounted(async () => {
  dependencyOptions.value = await fetchSystemData({ dictEncoding: 'dependency' })
  networkTypeOptions.value = await fetchSystemData({ dictEncoding: 'networkType' })
  if (props.currentData.deviceId) {
    form.value = { ...props.currentData };
  }
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.form-row {
  display: flex;
  justify-content: space-between;
  gap: 20px; /* 添加间距 */
}

.el-form-item {
  flex: 1;
}

.demo-form .el-form-item:nth-child(even) {
  margin-left: 20px;
}
</style>
