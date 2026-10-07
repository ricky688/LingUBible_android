package com.lingubible.app

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.AppwriteMaterialRepository
import com.lingubible.app.di.AppConfig
import io.appwrite.models.File
import io.appwrite.models.FileList
import io.appwrite.services.Storage
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppwriteMaterialRepositoryTest {

    private val clientProvider = mockk<AppwriteClientProvider>()
    private val storage = mockk<Storage>()
    private val appConfig = AppConfig(
        endpoint = "https://appwrite.lingubible.com/v1",
        projectId = "6a1097400037a55f6472",
        databaseId = "lingubible"
    )
    private lateinit var repository: AppwriteMaterialRepository

    @Before
    fun setup() {
        every { clientProvider.storage } returns storage
        every { clientProvider.appConfig } returns appConfig
        repository = AppwriteMaterialRepository(clientProvider)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    private fun mockFile(id: String, name: String, sizeOriginal: Long = 204800L): File {
        val file = mockk<File>()
        every { file.id } returns id
        every { file.name } returns name
        every { file.sizeOriginal } returns sizeOriginal
        return file
    }

    @Test
    fun `getPastPapers paginates and retrieves all papers beyond default 25 limit`() = runTest {
        // Page 1 has 100 items, Page 2 has 35 items
        val page1Files = (1..100).map { i ->
            mockFile("p1_$i", "CDS2004_24252_$i.pdf", 50000L)
        }
        val page2Files = (1..35).map { i ->
            mockFile("p2_$i", "CDS2004_23241_$i.pdf", 60000L)
        }

        coEvery {
            storage.listFiles(
                bucketId = "past_exam_papers",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(0)),
                search = "CDS2004"
            )
        } returns FileList(135, page1Files)

        coEvery {
            storage.listFiles(
                bucketId = "past_exam_papers",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(100)),
                search = "CDS2004"
            )
        } returns FileList(135, page2Files)

        val result = repository.getPastPapers("CDS2004")

        assertTrue(result.isSuccess)
        val papers = result.getOrThrow()
        assertEquals(135, papers.size)
        assertEquals("CDS2004", papers.first().courseCode)
        assertEquals("2024-2025", papers.first().academicYear)
        assertEquals("Term 2", papers.first().term)
        assertTrue(papers.first().viewUrl.contains("past_exam_papers/files/p1_1/view"))
        assertTrue(papers.first().downloadUrl.contains("past_exam_papers/files/p1_1/download"))
    }

    @Test
    fun `getPastPapers extracts academic years, terms, and instructor names reliably`() = runTest {
        val files = listOf(
            mockFile("f1", "CDS2004_24252.pdf", 1024L),
            mockFile("f2", "CDS2004_24251_Simmons.pdf", 2048L),
            mockFile("f3", "CDS2004_2425S.pdf", 4096L),
            mockFile("f4", "BUS1102_2022-2023_Term2.pdf", 8192L) // different course
        )

        coEvery {
            storage.listFiles(
                bucketId = "past_exam_papers",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getPastPapers("CDS2004")
        assertTrue(result.isSuccess)
        val papers = result.getOrThrow()

        assertEquals(3, papers.size)

        val paper1 = papers.find { it.id == "f1" }!!
        assertEquals("CDS2004", paper1.courseCode)
        assertEquals("2024-2025", paper1.academicYear)
        assertEquals("Term 2", paper1.term)
        assertNull(paper1.instructorName)
        assertEquals(1024L, paper1.fileSize)

        val paper2 = papers.find { it.id == "f2" }!!
        assertEquals("CDS2004", paper2.courseCode)
        assertEquals("2024-2025", paper2.academicYear)
        assertEquals("Term 1", paper2.term)
        assertEquals("Simmons", paper2.instructorName)
        assertEquals(2048L, paper2.fileSize)

        val paper3 = papers.find { it.id == "f3" }!!
        assertEquals("CDS2004", paper3.courseCode)
        assertEquals("2024-2025", paper3.academicYear)
        assertEquals("Summer Term", paper3.term)
        assertNull(paper3.instructorName)
    }

    @Test
    fun `getSyllabus resolves latest syllabus file by largest numeric suffix`() = runTest {
        val files = listOf(
            mockFile("syl_old", "CDS2004-202401.pdf", 100000L),
            mockFile("syl_newest", "CDS2004-202601.pdf", 120000L),
            mockFile("syl_mid", "CDS2004-202502.pdf", 110000L),
            mockFile("syl_other", "BUS1102-202601.pdf", 90000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(0)),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getSyllabus("CDS2004")
        assertTrue(result.isSuccess)

        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_newest", syllabus!!.id)
        assertEquals("CDS2004", syllabus.courseCode)
        assertEquals("CDS2004-202601.pdf", syllabus.fileName)
        assertEquals(120000L, syllabus.fileSize)
        assertEquals(
            "https://appwrite.lingubible.com/v1/storage/buckets/course_syllabus/files/syl_newest/view?project=6a1097400037a55f6472",
            syllabus.viewUrl
        )
        assertEquals(
            "https://appwrite.lingubible.com/v1/storage/buckets/course_syllabus/files/syl_newest/download?project=6a1097400037a55f6472",
            syllabus.downloadUrl
        )
    }

    @Test
    fun `getSyllabus enforces strict delimiter boundary to prevent course code prefix collisions`() = runTest {
        val files = listOf(
            mockFile("syl_collision", "ENG1001A-202601.pdf", 100000L),
            mockFile("syl_correct", "ENG1001-202501.pdf", 90000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(0)),
                search = "ENG1001"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getSyllabus("ENG1001")
        assertTrue(result.isSuccess)
        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_correct", syllabus!!.id)
        assertEquals("ENG1001", syllabus.courseCode)
    }

    @Test
    fun `getSyllabus handles unversioned file without treating course code numbers as version suffix`() = runTest {
        val files = listOf(
            mockFile("syl_unversioned", "CDS2426.pdf", 100000L),
            mockFile("syl_versioned", "CDS2426-2425.pdf", 95000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(0)),
                search = "CDS2426"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getSyllabus("CDS2426")
        assertTrue(result.isSuccess)
        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_versioned", syllabus!!.id)
    }

    @Test
    fun `getSyllabus paginates across pages when bucket has more than 100 files`() = runTest {
        val page1Files = (1..100).map { i ->
            mockFile("other_$i", "OTHER$i-202501.pdf", 50000L)
        }
        val page2Files = listOf(
            mockFile("syl_target", "VIS1001-202601.pdf", 60000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(0)),
                search = "VIS1001"
            )
        } returns FileList(101, page1Files)

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = listOf(io.appwrite.Query.limit(100), io.appwrite.Query.offset(100)),
                search = "VIS1001"
            )
        } returns FileList(101, page2Files)

        val result = repository.getSyllabus("VIS1001")
        assertTrue(result.isSuccess)
        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_target", syllabus!!.id)
        assertEquals("VIS1001", syllabus.courseCode)
    }

    @Test
    fun `getSyllabus returns null when no matching syllabus exists`() = runTest {
        val files = listOf(
            mockFile("syl_other", "BUS1102-202601.pdf")
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(1, files)

        val result = repository.getSyllabus("CDS2004")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getSyllabus handles exception from storage gracefully`() = runTest {
        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = any(),
                search = any()
            )
        } throws RuntimeException("Network error")

        val result = repository.getSyllabus("CDS2004")
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `storage null returns empty or null safely`() = runTest {
        every { clientProvider.storage } returns null
        val repoNoStorage = AppwriteMaterialRepository(clientProvider)

        val papersRes = repoNoStorage.getPastPapers("CDS2004")
        assertTrue(papersRes.isSuccess)
        assertTrue(papersRes.getOrThrow().isEmpty())

        val syllabusRes = repoNoStorage.getSyllabus("CDS2004")
        assertTrue(syllabusRes.isSuccess)
        assertNull(syllabusRes.getOrThrow())
    }

    @Test
    fun `getPastPapers sorts newest term first, sinks unknown to bottom, and filters non-PDFs`() = runTest {
        val files = listOf(
            mockFile("f_non_pdf", "CDS2004_preview.png"),
            mockFile("f_old", "CDS2004_21221.pdf"),
            mockFile("f_new", "CDS2004_24252.pdf"),
            mockFile("f_mid", "CDS2004_23241.pdf"),
            mockFile("f_unparsed", "CDS2004_special_exam.pdf")
        )

        coEvery {
            storage.listFiles(
                bucketId = "past_exam_papers",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getPastPapers("CDS2004")
        assertTrue(result.isSuccess)
        val papers = result.getOrThrow()

        // f_non_pdf should be filtered out
        assertEquals(4, papers.size)
        assertFalse(papers.any { it.id == "f_non_pdf" })

        // Order: f_new (2024-2025 Term 2), f_mid (2023-2024 Term 1), f_old (2021-2022 Term 1), f_unparsed (Unknown)
        assertEquals("f_new", papers[0].id)
        assertEquals("2024-2025", papers[0].academicYear)
        assertEquals("Term 2", papers[0].term)

        assertEquals("f_mid", papers[1].id)
        assertEquals("2023-2024", papers[1].academicYear)
        assertEquals("Term 1", papers[1].term)

        assertEquals("f_old", papers[2].id)
        assertEquals("2021-2022", papers[2].academicYear)
        assertEquals("Term 1", papers[2].term)

        assertEquals("f_unparsed", papers[3].id)
        assertEquals("Unknown", papers[3].term)
    }

    @Test
    fun `getSyllabus filters non-PDF files and selects newest PDF syllabus`() = runTest {
        val files = listOf(
            mockFile("syl_docx", "CDS2004-202602.docx", 200000L),
            mockFile("syl_pdf", "CDS2004-202601.pdf", 150000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getSyllabus("CDS2004")
        assertTrue(result.isSuccess)
        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_pdf", syllabus!!.id)
        assertEquals("CDS2004-202601.pdf", syllabus.fileName)
    }

    @Test
    fun `getSyllabus supports space-separated course code and version suffix`() = runTest {
        val files = listOf(
            mockFile("syl_space", "CDS2004 - 202601.pdf", 140000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "course_syllabus",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getSyllabus("CDS2004")
        assertTrue(result.isSuccess)
        val syllabus = result.getOrThrow()
        assertNotNull(syllabus)
        assertEquals("syl_space", syllabus!!.id)
        assertEquals("CDS2004", syllabus.courseCode)
    }

    @Test
    fun `getPastPapers includes space-separated past exam paper filenames`() = runTest {
        val files = listOf(
            mockFile("pp_space", "CDS2004 - 24251.pdf", 80000L)
        )

        coEvery {
            storage.listFiles(
                bucketId = "past_exam_papers",
                queries = any(),
                search = "CDS2004"
            )
        } returns FileList(files.size.toLong(), files)

        val result = repository.getPastPapers("CDS2004")
        assertTrue(result.isSuccess)
        val papers = result.getOrThrow()
        assertEquals(1, papers.size)
        assertEquals("pp_space", papers.first().id)
        assertEquals("CDS2004", papers.first().courseCode)
        assertEquals("Term 1", papers.first().term)
    }

    @Test
    fun `downloadFileBytes delegates to storage getFileDownload successfully`() = runTest {
        val dummyBytes = byteArrayOf(1, 2, 3, 4)
        coEvery {
            storage.getFileDownload(bucketId = "course_syllabus", fileId = "syl_123")
        } returns dummyBytes

        val result = repository.downloadFileBytes("course_syllabus", "syl_123")
        assertTrue(result.isSuccess)
        assertArrayEquals(dummyBytes, result.getOrThrow())
    }

    @Test
    fun `downloadFileBytes returns failure when storage throws exception`() = runTest {
        coEvery {
            storage.getFileDownload(bucketId = "past_exam_papers", fileId = "pp_123")
        } throws RuntimeException("Network error")

        val result = repository.downloadFileBytes("past_exam_papers", "pp_123")
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
