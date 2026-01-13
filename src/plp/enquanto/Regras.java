package plp.enquanto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;

import plp.enquanto.Linguagem.*;
import plp.enquanto.parser.EnquantoBaseListener;
import plp.enquanto.parser.EnquantoParser.*;

import static java.lang.Integer.parseInt;

public class Regras extends EnquantoBaseListener {
	private final Leia leia;
	private final Skip skip;
	private final Propriedades valores;

	private Programa programa;

	public Regras() {
		leia = new Leia();
		skip = new Skip();
		valores = new Propriedades();
	}

	public Programa getPrograma() {
		return programa;
	}

	@Override
	public void exitBool(BoolContext ctx) {
		valores.insira(ctx, new Booleano("verdadeiro".equals(ctx.getText())));
	}

	@Override
	public void exitLeia(LeiaContext ctx) {
		valores.insira(ctx, leia);
	}

	@Override
	public void exitSe(SeContext ctx) {
		List<BooleanoContext> bools = ctx.booleano();
		List<ComandoContext> cmds = ctx.comando();

		Comando senaoCmd = new Skip();
		if (cmds.size() > bools.size()) {
			senaoCmd = valores.pegue(cmds.get(cmds.size() - 1));
		}

		Comando current = senaoCmd;
		for (int i = bools.size() - 1; i >= 0; i--) {
			Bool cond = valores.pegue(bools.get(i));
			Comando entao = valores.pegue(cmds.get(i));
			current = new Se(cond, entao, current);
		}
		valores.insira(ctx, current);
	}

	@Override
	public void exitInteiro(InteiroContext ctx) {
		valores.insira(ctx, new Inteiro(parseInt(ctx.getText())));
	}

	@Override
	public void exitSkip(SkipContext ctx) {
		valores.insira(ctx, skip);
	}

	@Override
	public void exitEscreva(EscrevaContext ctx) {
		final Expressao exp = valores.pegue(ctx.expressao());
		valores.insira(ctx, new Escreva(exp));
	}

	@Override
	public void exitPrograma(ProgramaContext ctx) {
		final List<Comando> cmds = valores.pegue(ctx.seqComando());
		programa = new Programa(cmds);
		valores.insira(ctx, programa);
	}

	@Override
	public void exitId(IdContext ctx) {
		final String id = ctx.ID().getText();
		valores.insira(ctx, new Id(id));
	}

	@Override
	public void exitSeqComando(SeqComandoContext ctx) {
		final List<Comando> comandos = new ArrayList<>();
		for (ComandoContext c : ctx.comando()) {
			comandos.add(valores.pegue(c));
		}
		valores.insira(ctx, comandos);
	}

	@Override
	public void exitAtribuicao(AtribuicaoContext ctx) {
		List<String> ids = valores.pegue(ctx.listaId());
		List<Expressao> exps = valores.pegue(ctx.listaExpressao());
		valores.insira(ctx, new Atribuicao(ids, exps));
	}

	@Override
	public void exitListaId(ListaIdContext ctx) {
		List<String> ids = new ArrayList<>();
		for (var token : ctx.ID()) ids.add(token.getText());
		valores.insira(ctx, ids);
	}

	@Override
	public void exitListaExpressao(ListaExpressaoContext ctx) {
		List<Expressao> exps = new ArrayList<>();
		for (var e : ctx.expressao()) exps.add(valores.pegue(e));
		valores.insira(ctx, exps);
	}

	@Override
	public void exitBloco(BlocoContext ctx) {
		final List<Comando> cmds = valores.pegue(ctx.seqComando());
		valores.insira(ctx, new Bloco(cmds));
	}

	@Override
	public void exitOpBin(OpBinContext ctx) {
		final Expressao esq = valores.pegue(ctx.expressao(0));
		final Expressao dir = valores.pegue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final Expressao exp = switch (op) {
			case "*" -> new ExpMult(esq, dir);
			case "/" -> new ExpDiv(esq, dir);
			case "+" -> new ExpSoma(esq, dir);
			case "-" -> new ExpSub(esq, dir);
			case "^" -> new ExpPot(esq, dir);
			default  -> throw new RuntimeException("Operador desconhecido: " + op);
		};
		valores.insira(ctx, exp);
	}

	@Override
	public void exitEnquanto(EnquantoContext ctx) {
		final Bool condicao = valores.pegue(ctx.booleano());
		final Comando comando = valores.pegue(ctx.comando());
		valores.insira(ctx, new Enquanto(condicao, comando));
	}

	@Override
	public void exitELogico(ELogicoContext ctx) {
		final Bool esq = valores.pegue(ctx.booleano(0));
		final Bool dir = valores.pegue(ctx.booleano(1));
		valores.insira(ctx, new ELogico(esq, dir));
	}
	
	@Override
	public void exitOpLogico(OpLogicoContext ctx) {
		final Bool esq = valores.pegue(ctx.booleano(0));
		final Bool dir = valores.pegue(ctx.booleano(1));
		final String op = ctx.getChild(1).getText();
		final Bool res = switch(op) {
			case "ou" -> new OuLogico(esq, dir);
			case "xor" -> new XorLogico(esq, dir);
			default -> throw new RuntimeException("Operador logico desconhecido: " + op);
		};
		valores.insira(ctx, res);
	}

	@Override
	public void exitBoolPar(BoolParContext ctx) {
		final Bool booleano = valores.pegue(ctx.booleano());
		valores.insira(ctx, booleano);
	}

	@Override
	public void exitNaoLogico(NaoLogicoContext ctx) {
		final Bool b = valores.pegue(ctx.booleano());
		valores.insira(ctx, new NaoLogico(b));
	}

	@Override
	public void exitExpPar(ExpParContext ctx) {
		final Expressao exp = valores.pegue(ctx.expressao());
		valores.insira(ctx, exp);
	}

	@Override
	public void exitExiba(ExibaContext ctx) {
		if (ctx.TEXTO() != null) {
			String t = ctx.TEXTO().getText();
			String texto = t.substring(1, t.length() - 1);
			valores.insira(ctx, new Exiba(texto));
		} else {
			Expressao exp = valores.pegue(ctx.expressao());
			valores.insira(ctx, new Exiba(exp));
		}
	}

	@Override
	public void exitOpRel(OpRelContext ctx) {
		final Expressao esq = valores.pegue(ctx.expressao(0));
		final Expressao dir = valores.pegue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final Bool exp = switch (op) {
			case "="  -> new ExpIgual(esq, dir);
			case "<=" -> new ExpMenorIgual(esq, dir);
			case ">=" -> new ExpMaiorIgual(esq, dir);
			case "<"  -> new ExpMenor(esq, dir);
			case ">"  -> new ExpMaior(esq, dir);
			case "<>" -> new ExpDiferente(esq, dir);
			default   -> throw new RuntimeException("Operador relacional desconhecido: " + op);
		};
		valores.insira(ctx, exp);
	}
	
	@Override
	public void exitPara(ParaContext ctx) {
		String id = ctx.ID().getText();
		Expressao de = valores.pegue(ctx.expressao(0));
		Expressao ate = valores.pegue(ctx.expressao(1));
		Comando cmd = valores.pegue(ctx.comando());
		valores.insira(ctx, new Para(id, de, ate, cmd));
	}

	@Override
	public void exitRepita(RepitaContext ctx) {
		Expressao vezes = valores.pegue(ctx.expressao());
		Comando cmd = valores.pegue(ctx.comando());
		valores.insira(ctx, new Repita(vezes, cmd));
	}

	@Override
	public void exitEscolha(EscolhaContext ctx) {
		Expressao exp = valores.pegue(ctx.expressao());
		Map<Integer, Comando> casos = new HashMap<>();
		Comando padrao = null;

		for (CasoContext c : ctx.caso()) {
			Map.Entry<Integer, Comando> entry = valores.pegue(c);
			if (entry.getKey() == null) {
				padrao = entry.getValue();
			} else {
				casos.put(entry.getKey(), entry.getValue());
			}
		}
		valores.insira(ctx, new Escolha(exp, casos, padrao));
	}

	@Override
	public void exitCaso(CasoContext ctx) {
		Comando cmd = valores.pegue(ctx.comando());
		if (ctx.expressao() != null) {
			Expressao valExp = valores.pegue(ctx.expressao());
			int val = valExp.getValor();
			valores.insira(ctx, new AbstractMap.SimpleEntry<>(val, cmd));
		} else {
			valores.insira(ctx, new AbstractMap.SimpleEntry<>(null, cmd));
		}
	}
}
