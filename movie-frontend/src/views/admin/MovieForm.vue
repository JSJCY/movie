<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovieDetail, createMovie, updateMovie, listCategories, listActors, listDirectors } from '@/api/movie'
import type { Category, Actor, Director } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const movieId = Number(route.params.id)

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const categories = ref<Category[]>([])
const actors = ref<Actor[]>([])
const directors = ref<Director[]>([])

const form = reactive({
  title: '',
  originalTitle: '',
  releaseDate: '',
  duration: 0,
  country: '',
  language: '',
  description: '',
  posterUrl: '',
  trailerUrl: '',
  categoryIds: [] as number[],
  actors: [] as { actorId: number; characterName: string }[],
  directorIds: [] as number[],
})

onMounted(async () => {
  const [catRes, actRes, dirRes] = await Promise.all([
    listCategories(),
    listActors({ page: 1, size: 100 }),
    listDirectors({ page: 1, size: 100 }),
  ])
  categories.value = catRes.data || []
  actors.value = actRes.data.records || []
  directors.value = dirRes.data.records || []

  if (isEdit) {
    loading.value = true
    try {
      const movieRes = await getMovieDetail(movieId)
      const m = movieRes.data
      Object.assign(form, {
        title: m.title,
        originalTitle: m.originalTitle || '',
        releaseDate: m.releaseDate || '',
        duration: m.duration || 0,
        country: m.country || '',
        language: m.language || '',
        description: m.description || '',
        posterUrl: m.posterUrl || '',
        trailerUrl: m.trailerUrl || '',
        categoryIds: categories.value.filter(c => m.categories?.includes(c.name)).map(c => c.id),
        actors: m.actors?.map(a => ({ actorId: a.id, characterName: a.characterName })) || [],
        directorIds: m.directors?.map(d => d.id) || [],
      })
    } finally {
      loading.value = false
    }
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...form,
      releaseDate: form.releaseDate || null,
      duration: form.duration || null,
    }
    if (isEdit) {
      await updateMovie(movieId, data)
      ElMessage.success('更新成功')
    } else {
      await createMovie(data)
      ElMessage.success('创建成功')
      router.push('/admin/movies')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="movie-form" v-loading="loading">
    <h3>{{ isEdit ? '编辑电影' : '新增电影' }}</h3>

    <el-form ref="formRef" :model="form" label-width="100px" style="max-width: 800px; margin-top: 20px">
      <el-form-item label="电影名称" prop="title" :rules="[{ required: true, message: '请输入电影名称' }]">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="原始片名" prop="originalTitle">
        <el-input v-model="form.originalTitle" />
      </el-form-item>
      <el-row>
        <el-col :span="8">
          <el-form-item label="上映日期" prop="releaseDate">
            <el-date-picker v-model="form.releaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="片长(分钟)" prop="duration">
            <el-input-number v-model="form.duration" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="制作国家/地区" prop="country">
            <el-input v-model="form.country" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="语言" prop="language">
        <el-input v-model="form.language" />
      </el-form-item>
      <el-form-item label="剧情简介" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="海报URL" prop="posterUrl">
        <el-input v-model="form.posterUrl" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryIds">
        <el-select v-model="form.categoryIds" multiple placeholder="选择分类">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="导演" prop="directorIds">
        <el-select v-model="form.directorIds" multiple placeholder="选择导演">
          <el-option v-for="d in directors" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="演员">
        <div v-for="(item, idx) in form.actors" :key="idx" style="display: flex; gap: 8px; margin-bottom: 8px">
          <el-select v-model="item.actorId" placeholder="选择演员" style="width: 200px">
            <el-option v-for="a in actors" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
          <el-input v-model="item.characterName" placeholder="饰演角色" style="width: 160px" />
          <el-button @click="form.actors.splice(idx, 1)" type="danger" plain>删除</el-button>
        </div>
        <el-button @click="form.actors.push({ actorId: 0, characterName: '' })">添加演员</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        <el-button @click="router.push('/admin/movies')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.movie-form h3 {
  margin-bottom: 0;
}
</style>
