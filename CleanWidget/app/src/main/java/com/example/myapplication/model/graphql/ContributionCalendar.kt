package com.example.myapplication.model.graphql

data class GraphQLResponse(
    val data: UserData?
)

data class UserData(
    val user: UserContributionData
)

data class UserContributionData(
    val contributionsCollection: ContributionsCollection
)

data class ContributionsCollection(
    val contributionCalendar: ContributionCalendar
)

data class ContributionCalendar(
    val weeks: List<Week>
)

data class Week(
    val contributionDays: List<ContributionDay>
)

data class ContributionDay(
    val date: String,
    val contributionCount: Int
)
