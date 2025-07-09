package org.balanceus.topping.domain.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sggcode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SggCode {

	@Id
	@Column(name = "sgg_cd_5")
	private Integer sggCd5;

	@Column(name = "sgg_cd_nm")
	private String sggCdNm;

	@Column(name = "sgg_cd_nm_region")
	private String sggCdNmRegion;

	@Column(name = "sgg_cd_nm_city")
	private String sggCdNmCity;
}