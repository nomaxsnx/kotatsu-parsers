package org.koitharu.kotatsu.parsers.site.madara.pt


import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("BABELWUXIA", "Babelwuxia", "en")
internal class Babelwuxia(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.BABELWUXIA, "read.babelwuxia.com") {
	override val datePattern = "MMMM d, yyyy"
}