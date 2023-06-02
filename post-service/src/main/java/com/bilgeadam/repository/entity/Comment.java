package com.bilgeadam.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document
public class Comment extends Base{
    @Id
    private String id;
    private String userid;
    private String username;
    private String postid;
    private String comment;
    private List<String> subCommentid;
    private List<String> commentLikes;
    private List<String> commentDislikes;

}
