# 요구사항 정리
- 객체의 참조와 테이블의 외래 키를 매핑해서 객체에서는 참조를 사용하고 테이블에서는 외래 키를 사용할 수 있도록 한다.

## 공통 부문
- [X] 생성일자, 수정일자를 가진 Base Entity를 구성한다.
- [X] 모든 엔티티는 동등성을 보장한다.

## question
```sql
create table question
(
id         bigint generated by default as identity,
contents   clob,
created_at timestamp    not null,
deleted    boolean      not null,
title      varchar(100) not null,
updated_at timestamp,
writer_id  bigint,
primary key (id)
)

alter table question
  add constraint fk_question_writer
    foreign key (writer_id)
      references user
```
- [X] 질문을 생성한다.
- [X] 하나의 질문에는 여러개의 답변이 달릴 수 있다.
- [X] 질문 조회
    - [X] 질문 ID 값으로 조회한다.
    - [X] 글쓴이로 조회한다.
- [X] 질문 삭제
  - [X] 질문자가 쓴 글만 삭제가 가능하다.
  - [X] 질문에 달린 답변도 모두 삭제한다.

## answer
```sql
create table answer
(
    id          bigint generated by default as identity,
    contents    clob,
    created_at  timestamp not null,
    deleted     boolean   not null,
    question_id bigint,
    updated_at  timestamp,
    writer_id   bigint,
    primary key (id)
)

alter table answer
  add constraint fk_answer_to_question
    foreign key (question_id)
      references question

alter table answer
  add constraint fk_answer_writer
    foreign key (writer_id)
      references user
```
- [X] 답변을 생성한다.
- [X] 답변 조회
    - [X] 질문 ID 값으로 조회한다.
    - [X] 답변 ID 값으로 조회한다.
    - [X] 글쓴이로 조회한다.
- [X] 답변 삭제
  - [X] 질문자외 다른 사람이 답변을 단 경우 삭제할 수 없다.
  - [x] 질문 삭제시 함께 삭제된다.

## delete_history
```sql
create table delete_history
(
    id            bigint generated by default as identity,
    content_id    bigint,
    content_type  varchar(255),
    create_date   timestamp,
    deleted_by_id bigint,
    primary key (id)
)

alter table delete_history
  add constraint fk_delete_history_to_user
    foreign key (deleted_by_id)
      references user
```
- [X] 삭제 이력을 생성한다.
- [X] 삭제 이력에는 여러 종류의 엔티티가 등록 된다.
- [X] 삭제 이력 조회
  - [X] Content ID 값으로 조회한다
  - [X] 삭제자로 조회한다.

## user
```sql
create table user
(
    id         bigint generated by default as identity,
    created_at timestamp   not null,
    email      varchar(50),
    name       varchar(20) not null,
    password   varchar(20) not null,
    updated_at timestamp,
    user_id    varchar(20) not null,
    primary key (id)
)

alter table user
    add constraint UK_a3imlf41l37utmxiquukk8ajc unique (user_id)
```
- [X] 유저를 생성한다.
  - [X] 유저 ID는 중복을 허용하지 않는다. 
- [X] 유저 조회
  - [X] 유저 ID 값으로 조회한다.
  - [X] 유저 name 값으로 조회한다.
  - [X] 유저 email 값으로 조회한다.

## QnA 서비스 리팩토링
- [ ] 질문 데이터를 완전히 삭제하는 것이 아니라 데이터의 상태를 삭제 상태(deleted - boolean type)로 변경한다.
- [ ] 로그인 사용자와 질문한 사람이 같은 경우 삭제할 수 있다.
- [ ] 답변이 없는 경우 삭제가 가능하다.
- [ ] 질문자와 답변 글의 모든 답변자 같은 경우 삭제가 가능하다.
- [ ] 질문을 삭제할 때 답변 또한 삭제해야 하며, 답변의 삭제 또한 삭제 상태(deleted)를 변경한다.
- [ ] 질문자와 답변자가 다른 경우 답변을 삭제할 수 없다.
- [ ] 질문과 답변 삭제 이력에 대한 정보를 DeleteHistory를 활용해 남긴다.
