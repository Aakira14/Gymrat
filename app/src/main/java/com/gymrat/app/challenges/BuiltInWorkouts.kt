package com.gymrat.app.challenges

data class WorkoutSection(
    val title: String,
    val workouts: List<WorkoutChallenge>
)

data class WorkoutGroup(
    val title: String,
    val sections: List<WorkoutSection>
)

object BuiltInWorkouts {
    val groups: List<WorkoutGroup> = listOf(
        WorkoutGroup(
            title = "Dumbbell Exercises",
            sections = listOf(
                WorkoutSection(
                    title = "Upper Body",
                    workouts = listOf(
                        w("Dumbbell Bicep Curl"),
                        w("Hammer Curl"),
                        w("Concentration Curl"),
                        w("Dumbbell Bench Press"),
                        w("Incline Dumbbell Press"),
                        w("Dumbbell Fly"),
                        w("Dumbbell Shoulder Press"),
                        w("Arnold Press"),
                        w("Lateral Raise"),
                        w("Front Raise"),
                        w("Rear Delt Fly"),
                        w("Dumbbell Shrugs")
                    )
                ),
                WorkoutSection(
                    title = "Back",
                    workouts = listOf(
                        w("Dumbbell Row"),
                        w("Single Arm Row"),
                        w("Renegade Row")
                    )
                ),
                WorkoutSection(
                    title = "Legs",
                    workouts = listOf(
                        w("Goblet Squat"),
                        w("Dumbbell Lunges"),
                        w("Step-Ups"),
                        w("Romanian Deadlift (Dumbbell)"),
                        w("Dumbbell Calf Raises")
                    )
                ),
                WorkoutSection(
                    title = "Core",
                    workouts = listOf(
                        w("Russian Twists"),
                        w("Dumbbell Side Bend"),
                        w("Weighted Sit-ups")
                    )
                )
            )
        ),
        WorkoutGroup(
            title = "Barbell Exercises",
            sections = listOf(
                WorkoutSection(
                    title = "Barbell",
                    workouts = listOf(
                        w("Barbell Squat"),
                        w("Deadlift"),
                        w("Bench Press"),
                        w("Incline Bench Press"),
                        w("Overhead Press"),
                        w("Barbell Row"),
                        w("Hip Thrust"),
                        w("Barbell Curl"),
                        w("Skull Crushers")
                    )
                )
            )
        ),
        WorkoutGroup(
            title = "Machine Exercises",
            sections = listOf(
                WorkoutSection(
                    title = "Machine",
                    workouts = listOf(
                        w("Lat Pulldown"),
                        w("Seated Row"),
                        w("Chest Press Machine"),
                        w("Pec Deck Fly"),
                        w("Cable Crossover"),
                        w("Leg Press"),
                        w("Leg Extension"),
                        w("Leg Curl"),
                        w("Smith Machine Squat"),
                        w("Cable Tricep Pushdown"),
                        w("Cable Bicep Curl")
                    )
                )
            )
        ),
        WorkoutGroup(
            title = "Bodyweight (No Equipment)",
            sections = listOf(
                WorkoutSection(
                    title = "Basics",
                    workouts = listOf(
                        w("Push-ups"),
                        w("Pull-ups"),
                        w("Chin-ups"),
                        w("Squats"),
                        w("Lunges"),
                        w("Plank"),
                        w("Sit-ups"),
                        w("Crunches")
                    )
                ),
                WorkoutSection(
                    title = "Advanced",
                    workouts = listOf(
                        w("Burpees"),
                        w("Mountain Climbers"),
                        w("Jump Squats"),
                        w("Handstand Push-ups"),
                        w("Dips")
                    )
                )
            )
        ),
        WorkoutGroup(
            title = "Cardio Exercises",
            sections = listOf(
                WorkoutSection(
                    title = "Cardio",
                    workouts = listOf(
                        w("Running (Treadmill)"),
                        w("Cycling"),
                        w("Jump Rope"),
                        w("Rowing Machine"),
                        w("Stair Climber"),
                        w("Elliptical Trainer")
                    )
                )
            )
        )
    )

    val all: List<WorkoutChallenge> = groups.flatMap { it.sections }.flatMap { it.workouts }

    fun byId(id: String?): WorkoutChallenge? = all.firstOrNull { it.id == id }

    private fun w(title: String): WorkoutChallenge = WorkoutChallenge(
        id = title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_'),
        title = title
    )
}
