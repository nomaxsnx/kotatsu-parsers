package org.koitharu.kotatsu.parsers.site.tr

import androidx.collection.ArrayMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.PagedMangaParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.*
import org.koitharu.kotatsu.parsers.util.*
import java.text.SimpleDateFormat
import java.util.*

@MangaSourceParser("MANGAAY", "MangaAy", "tr")
class MangaAy(context: MangaLoaderContext) : PagedMangaParser(context, MangaSource.MANGAAY, 45) {

	override val sortOrders: Set<SortOrder> = EnumSet.of(SortOrder.UPDATED)

	override val configKeyDomain = ConfigKey.Domain("manga-ay.com")

	override suspend fun getListPage(
		page: Int,
		query: String?,
		tags: Set<MangaTag>?,
		sortOrder: SortOrder,
	): List<Manga> {
		val tag = tags.oneOrThrowIfMany()
		if (!query.isNullOrEmpty() || !tags.isNullOrEmpty()) {
			if (page > 1) {
				return emptyList()
			}
			val url = "https://$domain/arama"
			val doc = webClient.httpPost(
				url,
				mapOf(
					"title" to query?.urlEncoded().orEmpty(),
					"genres" to tag?.key.orEmpty(),
				),
			).parseHtml()
			return doc.select(".table tr").map { tr ->
				val a = tr.selectFirstOrThrow("a")
				val href = a.attrAsRelativeUrl("href")
				Manga(
					id = generateUid(href),
					url = href,
					publicUrl = a.attrAsAbsoluteUrl("href"),
					title = a.text(),
					coverUrl = "",
					altTitle = null,
					rating = RATING_UNKNOWN,
					tags = emptySet(),
					description = null,
					state = null,
					author = null,
					isNsfw = isNsfwSource,
					source = source,
				)
			}
		} else {
			val url = buildString {
				append("https://")
				append(domain)
				append("/seriler")
				if (page > 1) {
					append("/")
					append(page)
				}
			}
			val doc = webClient.httpGet(url).parseHtml().requireElementById("ecommerce-products")
			return doc.select(".card").map { div ->
				val a = div.selectFirstOrThrow("a")
				val href = a.attrAsRelativeUrl("href")
				Manga(
					id = generateUid(href),
					url = href,
					publicUrl = a.attrAsAbsoluteUrl("href"),
					title = div.selectLastOrThrow(".item-name").text(),
					coverUrl = div.selectFirst("img")?.src().orEmpty(),
					altTitle = null,
					rating = RATING_UNKNOWN,
					tags = emptySet(),
					description = null,
					state = null,
					author = null,
					isNsfw = isNsfwSource,
					source = source,
				)
			}
		}
	}

	private var tagCache: ArrayMap<String, MangaTag>? = null
	private val mutex = Mutex()

	override suspend fun getTags(): Set<MangaTag> {
		return getOrCreateTagMap().values.toSet()
	}

	private suspend fun getOrCreateTagMap(): Map<String, MangaTag> = mutex.withLock {
		tagCache?.let { return@withLock it }
		val tagMap = ArrayMap<String, MangaTag>()
		val tagElements = webClient.httpGet("https://$domain/arama").parseHtml()
			.requireElementById("genres").select("option")
		for (option in tagElements) {
			if (option.text().isEmpty()) continue
			tagMap[option.text()] = MangaTag(
				key = option.attr("value"),
				title = option.text(),
				source = source,
			)
		}
		tagCache = tagMap
		return@withLock tagMap
	}

	override suspend fun getDetails(manga: Manga): Manga {
		val doc = webClient.httpGet(manga.url.toAbsoluteUrl(domain)).parseHtml()
		val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT)
		val tagMap = getOrCreateTagMap()
		val tags = doc.select("P.card-text .bg-success").mapNotNullToSet { tagMap[it.text()] }
		return manga.copy(
			description = doc.selectFirst("p.card-text")?.html()?.substringAfterLast("<br>"),
			coverUrl = doc.selectFirst("div.align-items-center div.align-items-center img")?.src().orEmpty(),
			tags = tags,
			chapters = doc.requireElementById("sonyuklemeler").select("tbody tr")
				.mapChapters(reversed = true) { i, tr ->
					val a = tr.selectFirstOrThrow("a")
					val href = a.attrAsRelativeUrl("href")
					MangaChapter(
						id = generateUid(href),
						name = a.text(),
						number = i + 1,
						url = href,
						scanlator = null,
						uploadDate = dateFormat.tryParse(tr.selectFirstOrThrow("time").attr("datetime")),
						branch = null,
						source = source,
					)
				},
		)
	}

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		return doc.select("div.mt-2 img").map { img ->
			val url = img.src()?.toRelativeUrl(domain) ?: img.parseFailed("Image src not found")
			MangaPage(
				id = generateUid(url),
				url = url,
				preview = null,
				source = source,
			)
		}
	}
}
