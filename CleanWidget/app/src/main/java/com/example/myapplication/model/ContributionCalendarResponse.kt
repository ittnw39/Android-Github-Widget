package com.example.myapplication.model

import com.example.myapplication.model.graphql.Week

data class ContributionCalendarResponse(
    val data: UserData?
)

data class UserData(
    val user: UserContributionCollection?
)

data class UserContributionCollection(
    val contributionsCollection: ContributionsCollection?
)

data class ContributionsCollection(
    val contributionCalendar: ContributionCalendar?
)

data class ContributionCalendar(
    val totalContributions: Int,
    val weeks: List<Week>
)
