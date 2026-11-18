package com.devclimb.language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "language")
public class Language {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "lang_id")
	private Integer id;

	@Column(name = "lang_judge0", nullable = false)
	private Integer judge0Id;

	@Column(name = "lang_name", nullable = false, length = 20)
	private String name;

	protected Language() {
	}

	public Language(Integer judge0Id, String name) {
		this.judge0Id = judge0Id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public Integer getJudge0Id() {
		return judge0Id;
	}

	public String getName() {
		return name;
	}
}
