package qna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qna.domain.*;
import qna.exception.CannotDeleteException;
import qna.fixtures.AnswerTestFixture;
import qna.fixtures.QuestionTestFixture;
import qna.fixtures.UserTestFixture;
import qna.repository.QuestionRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {
    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private DeleteHistoryService deleteHistoryService;

    @InjectMocks
    private QnaService qnaService;

    private User writer;
    private Question question;
    private Answer answer;
    private Answer answer2;

    @BeforeEach
    public void setUp() throws Exception {
        writer = UserTestFixture.createUserWithId(1L);
        question = QuestionTestFixture.createQuestionWithIdAndWriter(1L, writer);
        answer = AnswerTestFixture.createAnswerWithIdAndWriterAndQuestion(1L, writer, question);
        answer2 = AnswerTestFixture.createAnswerWithIdAndWriterAndQuestion(2L, writer, question);
    }

    @Test
    public void delete_성공() throws Exception {
        when(questionRepository.findByIdAndDeletedFalse(question.getId())).thenReturn(Optional.of(question));

        assertThat(question.isDeleted()).isFalse();
        qnaService.deleteQuestion(writer, question.getId());

        assertThat(question.isDeleted()).isTrue();
        verifyDeleteHistories();
    }

    @Test
    public void delete_다른_사람이_쓴_글() throws Exception {
        when(questionRepository.findByIdAndDeletedFalse(question.getId())).thenReturn(Optional.of(question));
        User otherUser = UserTestFixture.createUserWithId(2L);

        assertThatThrownBy(() -> qnaService.deleteQuestion(otherUser, question.getId()))
                .isInstanceOf(CannotDeleteException.class);
    }

    @Test
    public void delete_성공_질문자_답변자_같음() throws Exception {
        when(questionRepository.findByIdAndDeletedFalse(question.getId())).thenReturn(Optional.of(question));

        qnaService.deleteQuestion(writer, question.getId());

        assertThat(question.isDeleted()).isTrue();
        assertThat(answer.isDeleted()).isTrue();
        verifyDeleteHistories();
    }

    @Test
    public void delete_답변_중_다른_사람이_쓴_글() throws Exception {
        User otherUser = UserTestFixture.createUserWithId(2L);
        Answer otherAnswer = new Answer(3L, otherUser, question, "Answers Contents1");
        when(questionRepository.findByIdAndDeletedFalse(this.question.getId())).thenReturn(Optional.of(this.question));

        assertThatThrownBy(() -> qnaService.deleteQuestion(writer, this.question.getId()))
                .isInstanceOf(CannotDeleteException.class);
    }

    private void verifyDeleteHistories() {
        DeleteHistories deleteHistories = new DeleteHistories(Arrays.asList(
                DeleteHistory.ofQuestion(question),
                DeleteHistory.ofAnswer(answer),
                DeleteHistory.ofAnswer(answer2)
        ));
        verify(deleteHistoryService).saveAll(deleteHistories);
    }
}
