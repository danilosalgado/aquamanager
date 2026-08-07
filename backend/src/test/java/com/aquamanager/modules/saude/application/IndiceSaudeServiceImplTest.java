package com.aquamanager.modules.saude.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.saude.infrastructure.persistence.IndiceSaudeTanqueRepository;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndiceSaudeServiceImplTest {

    @Mock private TanqueRepository tanqueRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private RegistroQualidadeAguaRepository qualidadeAguaRepository;
    @Mock private RegistroCrescimentoRepository crescimentoRepository;
    @Mock private RegistroMortalidadeRepository mortalidadeRepository;
    @Mock private RegistroAlimentacaoRepository alimentacaoRepository;
    @Mock private IndiceSaudeTanqueRepository indiceSaudeRepository;

    private IndiceSaudeServiceImpl service;

    private final UUID tanqueId = UUID.randomUUID();
    private Lote lote;
    private Especie especie;

    @BeforeEach
    void setUp() {
        service = new IndiceSaudeServiceImpl(tanqueRepository, loteRepository, qualidadeAguaRepository,
                crescimentoRepository, mortalidadeRepository, alimentacaoRepository, indiceSaudeRepository);

        especie = new Especie();
        especie.setTempMin(BigDecimal.valueOf(26));
        especie.setTempMax(BigDecimal.valueOf(30));
        especie.setPhMin(BigDecimal.valueOf(6.5));
        especie.setPhMax(BigDecimal.valueOf(8.5));
        especie.setOxigenioMin(BigDecimal.valueOf(4));

        lote = new Lote();
        lote.setEspecie(especie);
        lote.setQuantidadeAtual(1000);

        when(loteRepository.findByTanqueIdAndStatus(tanqueId, StatusLote.ATIVO)).thenReturn(List.of(lote));
        // "lenient": nem todo teste chega a percorrer estes pontos (ex.: sem lote ativo retorna cedo)
        lenient().when(mortalidadeRepository.findByLoteIdAndDataGreaterThanEqual(any(), any())).thenReturn(List.of());
        lenient().when(alimentacaoRepository.findByLoteIdAndHorarioBetween(any(), any(), any())).thenReturn(List.of());
        lenient().when(crescimentoRepository.findTop2ByLoteIdOrderByDataPesagemDesc(any())).thenReturn(List.of());
    }

    @Test
    void semLoteAtivoRetornaSemDadosSuficientes() {
        when(loteRepository.findByTanqueIdAndStatus(tanqueId, StatusLote.ATIVO)).thenReturn(List.of());

        var resultado = service.calcular(tanqueId);

        assertThat(resultado.semDadosSuficientes()).isTrue();
        assertThat(resultado.score()).isNull();
    }

    @Test
    void aguaDentroDaFaixaIdealGeraNotaExcelente() {
        RegistroQualidadeAgua registro = new RegistroQualidadeAgua();
        registro.setTemperatura(BigDecimal.valueOf(28));
        registro.setPh(BigDecimal.valueOf(7.2));
        registro.setOxigenioDissolvido(BigDecimal.valueOf(5));
        registro.setMedidoEm(Instant.now());
        when(qualidadeAguaRepository.findFirstByTanqueIdOrderByMedidoEmDesc(tanqueId)).thenReturn(Optional.of(registro));

        var resultado = service.calcular(tanqueId);

        assertThat(resultado.semDadosSuficientes()).isFalse();
        assertThat(resultado.score()).isEqualTo(100);
        assertThat(resultado.classificacao()).isEqualTo("EXCELENTE");
    }

    @Test
    void oxigenioAbaixoDoMinimoDerrubaSignificativamenteANota() {
        RegistroQualidadeAgua registro = new RegistroQualidadeAgua();
        registro.setTemperatura(BigDecimal.valueOf(28));
        registro.setPh(BigDecimal.valueOf(7.2));
        registro.setOxigenioDissolvido(BigDecimal.valueOf(1)); // bem abaixo do mínimo (4)
        registro.setMedidoEm(Instant.now());
        when(qualidadeAguaRepository.findFirstByTanqueIdOrderByMedidoEmDesc(tanqueId)).thenReturn(Optional.of(registro));

        var resultado = service.calcular(tanqueId);

        // Perde os 20 pontos de oxigênio de um total de 55 (temp 20 + oxigênio 20 + ph 15) => 35/55 ≈ 64
        assertThat(resultado.score()).isLessThan(80);
        assertThat(resultado.detalhes()).anyMatch(d -> d.contains("Oxigênio"));
    }

    @Test
    void mortalidadeElevadaReduzAClassificacaoParaCritico() {
        RegistroQualidadeAgua registro = new RegistroQualidadeAgua();
        registro.setTemperatura(BigDecimal.valueOf(15)); // fora da faixa
        registro.setPh(BigDecimal.valueOf(4)); // fora da faixa
        registro.setOxigenioDissolvido(BigDecimal.valueOf(1)); // abaixo do mínimo
        registro.setMedidoEm(Instant.now());
        when(qualidadeAguaRepository.findFirstByTanqueIdOrderByMedidoEmDesc(tanqueId)).thenReturn(Optional.of(registro));

        var mortalidade = new com.aquamanager.modules.mortalidade.domain.RegistroMortalidade();
        mortalidade.setQuantidade(500); // metade do lote morreu
        when(mortalidadeRepository.findByLoteIdAndDataGreaterThanEqual(any(), any())).thenReturn(List.of(mortalidade));

        var resultado = service.calcular(tanqueId);

        assertThat(resultado.classificacao()).isEqualTo("CRITICO");
        assertThat(resultado.score()).isLessThan(50);
    }
}
