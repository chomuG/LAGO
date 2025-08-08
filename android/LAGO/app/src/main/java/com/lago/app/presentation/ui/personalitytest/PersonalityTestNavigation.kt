package com.lago.app.presentation.ui.personalitytest

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

sealed class PersonalityTestStep {
    object TermsAgreement : PersonalityTestStep()
    object NicknameSetting : PersonalityTestStep()
    data class TestIntro(val nickname: String) : PersonalityTestStep()
    data class PersonalityTest(val nickname: String) : PersonalityTestStep()
    data class TestResult(val nickname: String, val score: Int) : PersonalityTestStep()
}

@Composable
fun PersonalityTestNavigation(
    onBackToHome: () -> Unit = {},
    onTestComplete: (PersonalityResult) -> Unit = {}
) {
    var currentStep by remember { mutableStateOf<PersonalityTestStep>(PersonalityTestStep.TermsAgreement) }
    var nickname by remember { mutableStateOf("") }
    var testScore by remember { mutableStateOf(0) }

    when (currentStep) {
        is PersonalityTestStep.TermsAgreement -> {
            TermsAgreementScreen(
                onBackClick = onBackToHome,
                onNextClick = {
                    currentStep = PersonalityTestStep.NicknameSetting
                }
            )
        }
        
        is PersonalityTestStep.NicknameSetting -> {
            NicknameSettingScreen(
                onBackClick = {
                    currentStep = PersonalityTestStep.TermsAgreement
                },
                onNextClick = { inputNickname ->
                    nickname = inputNickname
                    currentStep = PersonalityTestStep.TestIntro(inputNickname)
                }
            )
        }
        
        is PersonalityTestStep.TestIntro -> {
            PersonalityTestIntroScreen(
                nickname = (currentStep as PersonalityTestStep.TestIntro).nickname,
                onNextClick = {
                    currentStep = PersonalityTestStep.PersonalityTest(nickname)
                }
            )
        }
        
        is PersonalityTestStep.PersonalityTest -> {
            PersonalityTestScreen(
                nickname = (currentStep as PersonalityTestStep.PersonalityTest).nickname,
                onBackClick = {
                    currentStep = PersonalityTestStep.TestIntro(nickname)
                },
                onPreviousClick = {
                    currentStep = PersonalityTestStep.TestIntro(nickname)
                },
                onTestComplete = { score ->
                    testScore = score
                    currentStep = PersonalityTestStep.TestResult(nickname, score)
                }
            )
        }
        
        is PersonalityTestStep.TestResult -> {
            PersonalityTestResultScreen(
                nickname = (currentStep as PersonalityTestStep.TestResult).nickname,
                totalScore = (currentStep as PersonalityTestStep.TestResult).score,
                onCompleteClick = {
                    val personalityType = PersonalityTestData.calculatePersonality((currentStep as PersonalityTestStep.TestResult).score)
                    val characterRes = when (personalityType) {
                        PersonalityType.CAUTIOUS -> com.lago.app.R.drawable.character_green
                        PersonalityType.BALANCED -> com.lago.app.R.drawable.character_blue
                        PersonalityType.ACTIVE -> com.lago.app.R.drawable.character_yellow
                        PersonalityType.AGGRESSIVE -> com.lago.app.R.drawable.character_red
                    }
                    val result = PersonalityResult(
                        type = personalityType,
                        score = (currentStep as PersonalityTestStep.TestResult).score,
                        characterRes = characterRes,
                        description = PersonalityTestData.getPersonalityDescription(personalityType)
                    )
                    onTestComplete(result)
                }
            )
        }
    }
}