"""Regression checks for the local KUMA judge."""

import unittest

from kuma_eval import CASES, judge_responses


class JudgeTest(unittest.TestCase):
    def test_live_refusal_cases_do_not_pass_during_provider_outages(self):
        for case in CASES:
            if case.id not in {"BT-02", "BT-03", "BT-04"}:
                continue
            for message in (
                "Set DEEPSEEK_API_KEY to use AI commands.",
                "DeepSeek returned HTTP 401.",
                "DeepSeek returned HTTP 429.",
                "Could not reach DeepSeek.",
            ):
                with self.subTest(case=case.id, message=message):
                    status, details = judge_responses(
                        case, [[f"AI unavailable: {message}"], ["No actions recorded."]], live=True)
                    self.assertEqual("insufficient_evidence", status)
                    self.assertTrue(details)

    def test_valid_refusal_requires_empty_game_history(self):
        case = next(case for case in CASES if case.id == "BT-03")
        for message in (
            "AI did not propose a supported action. Try a more specific request.",
            "AI proposed an invalid game command.",
        ):
            with self.subTest(message=message):
                reply = [f"AI unavailable: {message}"]
                self.assertEqual("pass", judge_responses(
                    case, [reply, ["No actions recorded."]], live=True)[0])
                self.assertEqual("issue", judge_responses(
                    case, [reply, ["OK | inspect mill | Evidence discovered"]], live=True)[0])

    def test_live_dialogue_outage_is_insufficient_evidence(self):
        case = next(case for case in CASES if case.id == "BT-10")
        responses = [["AI dialogue unavailable: DeepSeek returned HTTP 503."]]
        self.assertEqual("insufficient_evidence", judge_responses(case, responses, live=True)[0])

    def test_malformed_model_reply_is_a_behavior_issue(self):
        case = next(case for case in CASES if case.id == "BT-02")
        status, details = judge_responses(
            case,
            [["AI unavailable: DeepSeek returned an invalid JSON response."], ["No actions recorded."]],
            live=True)
        self.assertEqual("issue", status)
        self.assertTrue(details)


if __name__ == "__main__":
    unittest.main()
