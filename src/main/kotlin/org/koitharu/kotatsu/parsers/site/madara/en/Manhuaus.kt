package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHUAUS", "ManhuaUs", "en")
internal class Manhuaus(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANHUAUS, "manhuaus.com")
