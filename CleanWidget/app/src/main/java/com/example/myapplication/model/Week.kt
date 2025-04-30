package com.example.myapplication.model

import com.example.myapplication.model.graphql.ContributionDay

data class Week(
    val contributionDays: List<ContributionDay>
)