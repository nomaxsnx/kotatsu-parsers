package org.koitharu.kotatsu.parsers.site.mangareader.ja

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.mangareader.MangaReaderParser
import java.util.Locale

@MangaSourceParser("MANGAJP", "MangaJp", "ja")
internal class MangaJp(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaSource.MANGAJP, "mangajp.top", pageSize = 54, searchPageSize = 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
