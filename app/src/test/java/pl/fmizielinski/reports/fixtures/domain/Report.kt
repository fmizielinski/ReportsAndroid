package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.Report

fun report(
    id: Int = 1,
    title: String = "title",
    description: String = "description",
    reportDate: String = "12 Jun",
    comments: Int = 0,
) = Report(
    id = id,
    title = title,
    description = description,
    reportDate = reportDate,
    comments = comments,
)
